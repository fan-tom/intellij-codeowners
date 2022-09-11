package com.github.fantom.codeowners

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.indexing.CodeownersEntryOccurrence
import com.github.fantom.codeowners.indexing.CodeownersFilesIndex
import com.github.fantom.codeowners.indexing.OwnerString
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.github.fantom.codeowners.services.CodeownersMatcher
import com.github.fantom.codeowners.settings.CodeownersSettings
import com.github.fantom.codeowners.util.*
import com.intellij.ProjectTopics
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileTypes.ExactFileNameMatcher
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.DumbService.DumbModeListener
import com.intellij.openapi.project.NoAccessDuringPsiEvents
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vcs.FileStatusManager
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsListener
import com.intellij.openapi.vcs.VcsRoot
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.ProjectScope
import com.intellij.util.Time
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.messages.Topic
import com.jetbrains.rd.util.concurrentMapOf

typealias OwnersList = List<OwnerString>
typealias OwnersSet = Set<OwnerString>

// data class OwnersSet(owners: Sequence<OwnerString>) {
//     val owners = owners.sortedWith()
//     override fun toString(): String {
//         return super.toString()
//     }
// }
/**
 * Class that represents a list of owners together with a link to exact offset in a CODEOWNERS file that assigns them.
 *
 * [owners] list is empty if reference points to an unset record
 */
data class OwnersReference(val owners: OwnersList = emptyList(), val offset: Int = 0)

/**
 * Represents a reference to given CODEOWNERS file together with reference to particular entry in this file, if any
 * Reference may be null if no entry in given CODEOWNERS file matches some file for which this reference was created
 */
data class OwnersFileReference(val url: String, val ref: OwnersReference?)

sealed class GetFileOwnersError {
    object InDumbMode : GetFileOwnersError() {
        override fun toString() = "dumb mode"
    }
    object NotInProject : GetFileOwnersError() {
        override fun toString() = "file not in project"
    }
    object Disposed : GetFileOwnersError() {
        override fun toString() = "application or project is disposed"
    }
    object NoVirtualFile : GetFileOwnersError() {
        override fun toString() = "codeowners file has no associated virtual file"
    }
}

/**
 * [CodeownersManager] handles CODEOWNERS files indexing and status caching.
 */
@Suppress("MagicNumber", "TooManyFunctions")
class CodeownersManager(private val project: Project) : DumbAware, Disposable {

    private val matcher = project.service<CodeownersMatcher>()
    private val settings = CodeownersSettings.getInstance()
    private val projectLevelVcsManager = ProjectLevelVcsManager.getInstance(project)

    private val debouncedStatusesChanged = object : Debounced<Any?>(1000) {
        override fun task(argument: Any?) {
            LOGGER.trace("debouncedStatusesChanged task")
            expiringStatusCache.clear()
            FileStatusManager.getInstance(project).fileStatusesChanged()
        }
    }.also {
        Disposer.register(this, it)
    }

    private val commonRunnableListeners = CommonRunnableListeners(debouncedStatusesChanged)
    private var messageBus = project.messageBus.connect(this)
    private val cachedCodeownersFilesIndex = CachedConcurrentMap
        .create<CodeownersFileType, List<CodeownersEntryOccurrence>?> { key ->
            CodeownersFilesIndex.getEntries(
                project,
                key
            )
        }

    private val expiringStatusCache =
        ExpiringMap<VirtualFile, Map<CodeownersFileType, OwnersFileReference>?>(Time.SECOND)

    private val debouncedExitDumbMode = object : Debounced<Boolean?>(3000) {
        override fun task(argument: Boolean?) {
            LOGGER.trace("debouncedExitDumbMode task")
            cachedCodeownersFilesIndex.clear()
            for ((key, value) in FILE_TYPES_ASSOCIATION_QUEUE) {
                associateFileType(key, value)
            }
            debouncedStatusesChanged.run()
        }
    }

    private var working = false

    // TODO: init with current value instead of relying to the fact that instance of this class is created
    // before VCS is taken into account and corresponding event sent
    private val vcsRoots = mutableListOf<VcsRoot>()

    /**
     * Checks if CODEOWNERS files watching is enabled.
     *
     * @return enabled
     */
    private val isEnabled
        get() = settings.codeownersFileStatus

    /** [VirtualFileListener] instance to check if file's content was changed. */
    private val bulkFileListener = object : BulkFileListener {
        override fun before(events: MutableList<out VFileEvent>) {
            events.forEach {
                handleEvent(it)
            }
        }

        private fun handleEvent(event: VFileEvent) {
            LOGGER.trace("handleEvent")
            val fileType = event.file?.fileType
            if (fileType is CodeownersFileType) {
                LOGGER.trace("handleEvent clear")
                cachedCodeownersFilesIndex.remove(fileType)
                expiringStatusCache.clear()
                debouncedStatusesChanged.run()
            }
        }
    }

    /** [CodeownersSettings] listener to watch changes in the plugin's settings. */
    private val settingsListener = CodeownersSettings.Listener { key, value ->
        when (key) {
            CodeownersSettings.KEY.CODEOWNERS_FILE_STATUS -> toggle(value as Boolean)
//            CodeownersSettings.KEY.HIDE_IGNORED_FILES -> ProjectView.getInstance(project).refresh()
            else -> {}
        }
    }

    init {
        toggle(isEnabled)
    }

    /**
     * @return: list of CODEOWNERS files that can assign owners to given file.
     * Might be no equal to the list of all existing CODEOWNERS files i.e. in case of local overrides for Bitbucket
     */
    @Suppress("UnusedPrivateMember")
    fun getApplicableCodeownersFiles(file: VirtualFile): Map<CodeownersFileType, List<CodeownersEntryOccurrence>> {
        // TODO handle actual overrides
        return FILE_TYPES
            .mapNotNull { ty -> cachedCodeownersFilesIndex[ty]?.let { Pair(ty, it) } }
            .associate { it }
    }

    /**
     * Checks if file has owners.
     *
     * @param file current file
     * @return file owners by codeowner file type or error.
     * Empty if no applicable CODEOWNERS files exist
     * Each value of returned map contains a list of owners (if any) and reference to line in corresponding CODEOWNERS file
     * (or first line if no owners defined in given file)
     * Error if cannot retrieve them due to project or IDE state (dumb mode, disposed)
     */
    @Suppress("ComplexCondition", "ComplexMethod", "NestedBlockDepth", "ReturnCount")
    fun getFileOwners(file: VirtualFile): Either<GetFileOwnersError, Map<CodeownersFileType, OwnersFileReference>> {
        LOGGER.trace(">getFileOwners ${file.name}")
        expiringStatusCache[file]?.let {
            LOGGER.trace("<getFileOwners ${file.name} cached ${it.entries.joinToString(",")}")
            return Right(it)
        }
        if (DumbService.isDumb(project)) {
            LOGGER.trace("<getFileOwners ${file.name} dumbMode, null")
            return Left(GetFileOwnersError.InDumbMode)
        }
        if (!Utils.isInProject(file, project)) {
            LOGGER.trace("<getFileOwners ${file.name} not in project, emptyList")
            return Left(GetFileOwnersError.NotInProject)
        }
        if (ApplicationManager.getApplication().isDisposed || project.isDisposed ||
            /*|| !isEnabled  || */
            NoAccessDuringPsiEvents.isInsideEventProcessing()
        ) {
            LOGGER.trace("<getFileOwners ${file.name} emptyList")
            return Left(GetFileOwnersError.Disposed)
        }
        val ownersMap = mutableMapOf<CodeownersFileType, OwnersFileReference>()
//        var matched = false
        for ((fileType, codeownersFiles) in getApplicableCodeownersFiles(file)) {
            ProgressManager.checkCanceled()
//            if (CodeownersBundle.ENABLED_LANGUAGES[fileType] != true) {
//                continue
//            }
//            val codeownersFiles = cachedCodeownersFilesIndex[fileType] ?: continue
            val filesCount = codeownersFiles.size
            LOGGER.trace(">>getFileOwners ${file.name}, inspecting $fileType with $filesCount codeowners files")

            @Suppress("LoopWithTooManyJumpStatements")
            for (codeownersFile in codeownersFiles) {
                ProgressManager.checkCanceled()
                getFileOwners(file, codeownersFile).map {
                    ownersMap[fileType] = it ?: return@map
                }
            }
        }
        LOGGER.trace("<getFileOwners ${file.name} '${ownersMap.entries.joinToString(",")}'")
        expiringStatusCache[file] = ownersMap
        return Right(ownersMap)
    }

    @Suppress("ReturnCount")
    private fun getFileOwners(file: VirtualFile, codeownersFile: CodeownersEntryOccurrence):
        Either<GetFileOwnersError, OwnersFileReference?> {
        val codeownersVirtualFile = codeownersFile.file ?: return Left(GetFileOwnersError.NoVirtualFile)
        val relativePath = getRelativePathToTheNearestVcsRoot(file, codeownersVirtualFile) ?: return Right(null)

        return codeownersFile.items.lastOrNull {
            LOGGER.trace(">>>getFileOwners check pattern ${it.first} against $relativePath")
            val pattern = Glob.getPattern(it.first.pattern)
//                if (
            return@lastOrNull matcher.match(pattern, relativePath)
//                ) {
//                    true
//                        matched = true
//                }
//                return@lastOrNull false
        }?.let {
            LOGGER.trace("<<<getFileOwners pattern ${it.first} matches $relativePath")
            return@let Right(OwnersFileReference(codeownersVirtualFile.url, it.second))
        }
            ?: file.parent.let { directory ->
                vcsRoots.forEach { vcsRoot ->
                    ProgressManager.checkCanceled()
                    if (directory == vcsRoot.path) {
                        return@let Right(OwnersFileReference(codeownersVirtualFile.url, null))
                    }
                }
                return@let getFileOwners(file.parent, codeownersFile)
            }
    }

    @Suppress("ReturnCount")
    private fun getRelativePathToTheNearestVcsRoot(file: VirtualFile, codeownersVirtualFile: VirtualFile): String? {
        var relativePath = getRoot(codeownersVirtualFile)?.let {
            Utils.getRelativePath(it, file)
        } ?: return null

        relativePath =
            StringUtil.trimEnd(StringUtil.trimStart(relativePath, VfsUtilCore.VFS_SEPARATOR), VfsUtilCore.VFS_SEPARATOR)
        if (StringUtil.isEmpty(relativePath)) {
            return null
        }
        if (file.isDirectory) {
            relativePath += "/"
        }
        return relativePath
    }

    private fun getRoot(codeownersVirtualFile: VirtualFile): VirtualFile? {
        val vcsRoot = vcsRoots
            .minByOrNull {
                // TODO consider VcsUtils.getVscRootFor(Project, VirtualFile)
                VfsUtilCore.getRelativePath(it.path, codeownersVirtualFile)?.run {
                    split(VfsUtilCore.VFS_SEPARATOR_CHAR).count()
                } ?: 0
            } ?: return null
        return (codeownersVirtualFile.fileType as CodeownersFileType).getRoot(vcsRoot, codeownersVirtualFile)
    }

    val isAvailable: Boolean get() = working && codeownersFilesExist() ?: false

    @Suppress("ReturnCount")
    private fun codeownersFilesExist(): Boolean? {
        return CodeownersBundle.LANGUAGES
            .asSequence()
            .map { it.filename }
            .distinctBy { it }
            .any {
                FilenameIndex.getVirtualFilesByName(it, ProjectScope.getProjectScope(project)).isNotEmpty()
            }
//        val projectDir = project.guessProjectDir() ?: return null
//        val directory = PsiManager.getInstance(project).findDirectory(projectDir) ?: return null
//
//        val filename = CodeownersFileType.INSTANCE.codeownersLanguage.filename
//        val file = directory.findFile(filename)
//
//        return file?.virtualFile ?: directory.virtualFile.findChild(filename) != null
    }

    /** Enable manager. */
    private fun enable() {
        if (working) {
            return
        }
        settings.addListener(settingsListener)

        messageBus.subscribe(
            VirtualFileManager.VFS_CHANGES,
            bulkFileListener
        )
        messageBus.subscribe(
            ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED,
            VcsListener {
                vcsRoots.clear()
                vcsRoots.addAll(projectLevelVcsManager.allVcsRoots)
            }
        )
        messageBus.subscribe(
            DumbService.DUMB_MODE,
            object : DumbModeListener {
                override fun enteredDumbMode() = Unit
                override fun exitDumbMode() {
                    debouncedExitDumbMode.run()
                }
            }
        )
        messageBus.subscribe(ProjectTopics.PROJECT_ROOTS, commonRunnableListeners)
        messageBus.subscribe(RefreshStatusesListener.REFRESH_STATUSES, commonRunnableListeners)
        messageBus.subscribe(ProjectTopics.MODULES, commonRunnableListeners)
        working = true
    }

    /** Disable manager. */
    private fun disable() {
        settings.removeListener(settingsListener)
        working = false
    }

    override fun dispose() {
//        disable()
        LOGGER.trace("dispose")
        cachedCodeownersFilesIndex.clear()
    }

    /**
     * Runs [.enable] or [.disable] depending on the passed value.
     *
     * @param enable or disable
     */
    private fun toggle(enable: Boolean) {
        if (enable) {
            enable()
        } else {
            disable()
        }
    }

    fun interface RefreshStatusesListener {

        fun refresh()

        companion object {
            /** Topic to refresh files statuses using [MessageBusConnection]. */
            val REFRESH_STATUSES = Topic("Refresh files statuses", RefreshStatusesListener::class.java)
        }
    }

    companion object {
        private val LOGGER = Logger.getInstance(CodeownersManager::class.java)

        /** List of all available [CodeownersFileType]. */
        private val FILE_TYPES = CodeownersBundle.LANGUAGES.map(CodeownersLanguage::fileType)
//
        /** List of filenames that require to be associated with specific [CodeownersFileType]. */
        val FILE_TYPES_ASSOCIATION_QUEUE = concurrentMapOf<String, CodeownersFileType>()

        /**
         * Associates given file with proper [CodeownersFileType].
         *
         * @param fileName to associate
         * @param fileType file type to bind with pattern
         */
        fun associateFileType(fileName: String, fileType: CodeownersFileType) {
            val application = ApplicationManager.getApplication()
            if (application.isDispatchThread) {
                val fileTypeManager = FileTypeManager.getInstance()
                application.invokeLater(
                    {
                        application.runWriteAction {
                            fileTypeManager.associate(fileType, ExactFileNameMatcher(fileName))
                            FILE_TYPES_ASSOCIATION_QUEUE.remove(fileName)
                        }
                    },
                    ModalityState.NON_MODAL
                )
            } else if (!FILE_TYPES_ASSOCIATION_QUEUE.containsKey(fileName)) {
                FILE_TYPES_ASSOCIATION_QUEUE[fileName] = fileType
            }
        }
    }
}
