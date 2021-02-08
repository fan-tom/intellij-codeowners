package com.github.fantom.codeowners

import com.github.fantom.codeowners.indexing.CodeownersFilesIndex
import com.github.fantom.codeowners.services.CodeownersMatcher
import com.github.fantom.codeowners.util.*
import com.intellij.ProjectTopics
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
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
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.Time
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.messages.Topic
import com.github.fantom.codeowners.settings.CodeownersSettings
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.fileTypes.ExactFileNameMatcher
import com.intellij.openapi.fileTypes.FileTypeManager
import com.jetbrains.rd.util.concurrentMapOf

/**
 * [CodeownersManager] handles ignore files indexing and status caching.
 */
@Suppress("MagicNumber")
class CodeownersManager(private val project: Project) : DumbAware, Disposable {

    private val matcher = project.service<CodeownersMatcher>()
    private val settings = CodeownersSettings.getInstance()
    private val projectLevelVcsManager = ProjectLevelVcsManager.getInstance(project)

    private val debouncedStatusesChanged = object : Debounced<Any?>(1000) {
        override fun task(argument: Any?) {
            println("debouncedStatusesChanged task")
            expiringStatusCache.clear()
            FileStatusManager.getInstance(project).fileStatusesChanged()
        }
    }.also {
        Disposer.register(this, it)
    }

    private val commonRunnableListeners = CommonRunnableListeners(debouncedStatusesChanged)
    private var messageBus = project.messageBus.connect(this)
    private val cachedCodeownersFilesIndex = CachedList { CodeownersFilesIndex.getEntries(project) }

    private val expiringStatusCache = ExpiringMap<VirtualFile, List<String>?>(Time.SECOND)

    private val debouncedExitDumbMode = object : Debounced<Boolean?>(3000) {
        override fun task(argument: Boolean?) {
            println("debouncedExitDumbMode task")
            cachedCodeownersFilesIndex.clear()
            for ((key, value) in FILE_TYPES_ASSOCIATION_QUEUE) {
                associateFileType(key, value)
            }
            debouncedStatusesChanged.run()
        }
    }

    private var working = false
    private val vcsRoots = mutableListOf<VcsRoot>()
//
    /**
     * Checks if ignored files watching is enabled.
     *
     * @return enabled
     */
    private val isEnabled
        get() = settings.ignoredFileStatus

    /** [VirtualFileListener] instance to check if file's content was changed. */
    private val bulkFileListener = object : BulkFileListener {
        override fun before(events: MutableList<out VFileEvent>) {
            events.forEach {
                handleEvent(it)
            }
        }

        private fun handleEvent(event: VFileEvent) {
            println("handleEvent")
            val fileType = event.file?.fileType
            if (fileType is CodeownersFileType) {
                println("handleEvent clear")
                cachedCodeownersFilesIndex.clear()
                expiringStatusCache.clear()
                debouncedStatusesChanged.run()
            }
        }
    }

    /** [CodeownersSettings] listener to watch changes in the plugin's settings. */
    private val settingsListener = CodeownersSettings.Listener { key, value ->
        when (key) {
            CodeownersSettings.KEY.IGNORED_FILE_STATUS -> toggle(value as Boolean)
//            CodeownersSettings.KEY.HIDE_IGNORED_FILES -> ProjectView.getInstance(project).refresh()
            else -> {}
        }
    }

    init {
        toggle(isEnabled)
    }

    /**
     * Checks if file has owners.
     *
     * @param file current file
     * @return file owners list or null if cannot retrieve them due to project or IDE state (dumb mode, disposed)
     */
    @Suppress("ComplexCondition", "ComplexMethod", "NestedBlockDepth", "ReturnCount")
    fun getFileOwners(file: VirtualFile): List<String>? {
        println(">getFileOwners ${file.name}")
        expiringStatusCache[file]?.let {
            println("<getFileOwners ${file.name} cached ${it.joinToString(",")}")
            return it
        }
        if (DumbService.isDumb(project)) {
            println("<getFileOwners ${file.name} dumbMode, null")
            return null
        }
        if (!Utils.isInProject(file, project)) {
            println("<getFileOwners ${file.name} not in project, emptyList")
            return emptyList()
        }
        if (ApplicationManager.getApplication().isDisposed || project.isDisposed ||
                /*|| !isEnabled  || */
                NoAccessDuringPsiEvents.isInsideEventProcessing()
        ) {
            println("<getFileOwners ${file.name} emptyList")
            return emptyList()
        }
        var owners: List<String>? = null
//        var matched = false
//        for (fileType in FILE_TYPES) {
        ProgressManager.checkCanceled()
//            if (CodeownersBundle.ENABLED_LANGUAGES[fileType] != true) {
//                continue
//            }
        val valuesCount = cachedCodeownersFilesIndex.size
        println(">getFileOwners ${file.name}, inspecting CODEOWNERS with $valuesCount entries")

        @Suppress("LoopWithTooManyJumpStatements")
        for (value in cachedCodeownersFilesIndex) {
            ProgressManager.checkCanceled()
            val entryFile = value.file
            var relativePath = if (entryFile == null) {
                continue
            } else {
                Utils.getRelativePath(entryFile.parent, file)
            } ?: continue

            relativePath = StringUtil.trimEnd(StringUtil.trimStart(relativePath, "/"), "/")
            if (StringUtil.isEmpty(relativePath)) {
                continue
            }
            if (file.isDirectory) {
                relativePath += "/"
            }
            owners = value.items.lastOrNull {
                println(">getFileOwners check pattern ${it.first} against $relativePath")
                val pattern = Glob.getPattern(it.first)
//                if (
                return@lastOrNull matcher.match(pattern, relativePath)
//                ) {
//                    true
//                        matched = true
//                }
//                return@lastOrNull false
            }?.let {
                println(">getFileOwners pattern ${it.first} matches $relativePath")
                return@let it.second
            }
            if (owners != null) {
                break
            }
        }
//        }
        val res = if (/*valuesCount > 0*/ /*&& !ignored  &&*/ owners == null) {
            file.parent.let { directory ->
                vcsRoots.forEach { vcsRoot ->
                    ProgressManager.checkCanceled()
                    if (directory == vcsRoot.path) {
                        return@let null
//                        return expiringStatusCache.set(file, null)
                    }
                }
//                expiringStatusCache.set(file,
                return@let getFileOwners(file.parent)
//                )
            }
        } else {
            owners
        } ?: emptyList()
        println("<getFileOwners ${file.name} ${res.joinToString(",")}")
        return expiringStatusCache.set(file, res)
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
        println("dispose")
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
//        /** List of all available [CodeownersFileType]. */
//        private val FILE_TYPES = CodeownersBundle.LANGUAGES.map(CodeownersLanguage::fileType)
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
