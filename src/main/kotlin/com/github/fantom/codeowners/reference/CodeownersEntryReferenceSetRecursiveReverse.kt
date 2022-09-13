package com.github.fantom.codeowners.reference

import com.github.fantom.codeowners.lang.CodeownersEntryBase
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.github.fantom.codeowners.services.PatternCache
import com.github.fantom.codeowners.util.Glob
import com.github.fantom.codeowners.util.TimeTracerStub
import com.github.fantom.codeowners.util.withNullableCloseable
import com.intellij.icons.AllIcons
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiManager
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopes
import com.intellij.psi.search.scope.packageSet.FilteredNamedScope
import com.intellij.vcsUtil.VcsUtil

class CodeownersEntryReferenceSetRecursiveReverse(
    element: CodeownersEntryBase,
    val tracerStub: TimeTracerStub? = null
) :
    FileReferenceSet(element) {
    private val myCodeownersPatternsMatchedFilesCache: CodeownersPatternsMatchedFilesCache
    private val myPatternCache: PatternCache

    init {
        myCodeownersPatternsMatchedFilesCache = CodeownersPatternsMatchedFilesCache.getInstance(element.project)
        myPatternCache = PatternCache.getInstance(element.project)
    }

    /**
     * Creates [CodeownersEntryReference] instance basing on passed text value.
     *
     * @param range text range
     * @param index start index
     * @param text  string text
     * @return file reference
     */
    override fun createFileReference(range: TextRange, index: Int, text: String): FileReference {
        return CodeownersEntryReference(this, range, index, text)
    }

    /**
     * Sets ending slash as allowed.
     *
     * @return `false`
     */
    override fun isEndingSlashNotAllowed(): Boolean {
        return false
    }

    /**
     * Computes current element's parent context.
     *
     * @return contexts collection
     */
    override fun computeDefaultContexts(): Collection<PsiFileSystemItem> {
        val codeownersFile = element.containingFile
        var containingDirectory = codeownersFile.parent ?: codeownersFile.originalFile.containingDirectory
        if (containingDirectory == null) {
            val language = codeownersFile.language
            if (language is CodeownersLanguage) {
                val affectedRoot = language.fileType.getRoot(codeownersFile.originalFile.virtualFile)
//                if (affectedRoot != null) {
                containingDirectory = codeownersFile.manager.findDirectory(affectedRoot)
//                }
            }
        }
        return containingDirectory?.let { listOf(it) } ?: super.computeDefaultContexts()
    }

    override fun getReferenceCompletionFilter(): Condition<PsiFileSystemItem> {
        return Condition { item: PsiFileSystemItem ->
            val project = item.project
            val originalFile = element.containingFile.originalFile
            val codeownersFile = originalFile.virtualFile
            val language = originalFile.language as? CodeownersLanguage ?: return@Condition false
            val codeownersFileAffectedRoot = language.fileType.getRoot(codeownersFile)
            val codeownersFileVcsRoot = VcsUtil.getVcsRootFor(project, codeownersFileAffectedRoot)
                ?: return@Condition false
            isFileUnderSameVcsRoot(project, codeownersFileVcsRoot, item.virtualFile)
        }
    }

    /**
     * Returns last reference of the current element's references.
     *
     * @return last [FileReference]
     */
    override fun getLastReference(): FileReference? {
        val lastReference = super.getLastReference()
        return if (lastReference != null && lastReference.canonicalText.endsWith(separatorString)) {
            if (myReferences != null && myReferences.size > 1) myReferences[myReferences.size - 2] else null
        } else lastReference
    }

    /**
     * Disallows conversion to relative reference.
     *
     * @param relative is ignored
     * @return `false`
     */
    override fun couldBeConvertedTo(relative: Boolean): Boolean {
        return false
    }

    /**
     * Parses entry, searches for file references and stores them in [.myReferences].
     */
    override fun reparse() {
        ProgressManager.checkCanceled()
        val tracer = tracerStub?.start("reparse $pathString")
        withNullableCloseable(tracer) {
            val str = StringUtil.trimEnd(pathString, separatorString)
            val referencesList = mutableListOf<FileReference?>()
            val separatorString = separatorString // save func calls
            val sepLen = separatorString.length // separator's length can be more than 1 char
            var currentSlash = -sepLen
            val startInElement = startInElement

            // skip white space
            while (currentSlash + sepLen < str.length && Character.isWhitespace(str[currentSlash + sepLen])) {
                currentSlash++
            }
            if (currentSlash + sepLen + sepLen < str.length &&
                str.substring(currentSlash + sepLen, currentSlash + sepLen + sepLen) == separatorString
            ) {
                currentSlash += sepLen
            }

            var index = 0
            if (str == separatorString) {
                val fileReference = createFileReference(
                    TextRange(startInElement, startInElement + sepLen),
                    index++,
                    separatorString
                )
                referencesList.add(fileReference)
            }
            while (true) {
                ProgressManager.checkCanceled()
                val nextSlash = str.indexOf(separatorString, currentSlash + sepLen)
                val subReferenceText = if (nextSlash > 0) str.substring(0, nextSlash) else str
                val range = TextRange(
                    startInElement + currentSlash + sepLen,
                    startInElement + if (nextSlash > 0) nextSlash else str.length
                )
                val ref = createFileReference(range, index++, subReferenceText)
                referencesList.add(ref)
                currentSlash = nextSlash
                if (currentSlash < 0) {
                    break
                }
            }
            myReferences = referencesList.toTypedArray()
        }
    }

    override fun getNewAbsolutePath(root: PsiFileSystemItem, relativePath: String): String {
        val codeownersFile = containingFile
        val rootVF = root.virtualFile
        if (rootVF != null && codeownersFile?.virtualFile?.parent?.let { it != rootVF } == true) {
            rootVF.findFileByRelativePath(relativePath)?.also { relativeFile ->
                VfsUtilCore.getRelativePath(relativeFile, codeownersFile.virtualFile.parent)?.also {
                    return if (absoluteUrlNeedsStartSlash()) "/$it" else it
                }
            }
        }
        return super.getNewAbsolutePath(root, relativePath)
    }

    /**
     * Custom definition of [FileReference].
     */
    private inner class CodeownersEntryReference constructor(
        fileReferenceSet: FileReferenceSet,
        range: TextRange,
        index: Int,
        text: String
    ) : FileReference(fileReferenceSet, range, index, text) {
        // TODO unify respecting excluded/ignored project files among usages of different indexes
        inner class Resolver {
            val psiManager = element.manager
            private fun Collection<VirtualFile>.toResolveResults() = this
                .mapNotNull { getPsiFileSystemItem(psiManager, it)?.let(::PsiElementResolveResult) }
            private fun CharSequence.containsMetasymbols() = this.any { it == '*' || it == '?' }
            // (prefix, null) if no delimiter found
            // (prefix, "") if delimiter found at the end of the string
            // (prefix, suffix) otherwise
            private fun CharSequence.splitLast(delimiter: Char): Pair<CharSequence, CharSequence?> {
                return when (val lastIdx = this.lastIndexOf(delimiter)) {
                    -1 -> this to null
                    lastIndex -> this.subSequence(0, lastIdx) to ""
                    else -> this.subSequence(0, lastIdx) to this.subSequence(lastIdx + 1, length)
                }
            }

            private fun resolveFragment(
                text: CharSequence,
                context: PsiFileSystemItem,
                result: MutableCollection<ResolveResult>,
                caseSensitive: Boolean,
                dirOnly: Boolean
            ) {
                val project = context.project
                val currentDir = context.virtualFile

                val unescapedText = Glob.unescape(text)

                if (unescapedText == "**") {
                    val iterator: (VirtualFile) -> Boolean = iter@{
                        if (dirOnly && !it.isDirectory) {
                            return@iter true
                        }
                        if (it != currentDir) {
                            result.add(
                                PsiElementResolveResult(
                                    getPsiFileSystemItem(psiManager, it) ?: return@iter true
                                )
                            )
                        }
                        true
                    }

                    val projectFileIndex = ProjectRootManager.getInstance(project).fileIndex
                    projectFileIndex.iterateContentUnderDirectory(currentDir, iterator)

//                        VfsUtilCore.iterateChildrenRecursively(
//                            context.virtualFile,
//                            null,
//                            iterator,
//                            VirtualFileVisitor.SKIP_ROOT
//                        )
                } else if (unescapedText.containsMetasymbols()) {
                    // simply find children matching regex?
                    val regex = myPatternCache.createRelativePattern(unescapedText, caseSensitive)
                    result.addAll(
                        currentDir.children
                            .filter {
                                if (dirOnly && !it.isDirectory) {
                                    false
                                } else {
                                    it.name.matches(regex)
                                }
                            }
                            .toResolveResults()
                    )
                } else {
                    // simply find children with given name
                    // let parent class handle basic case
                    if (dirOnly) {
                        val resolveResults = mutableListOf<ResolveResult>()
                        super@CodeownersEntryReference.innerResolveInContext(unescapedText.toString(), context, resolveResults, caseSensitive)
                        result.addAll(
                            resolveResults
                                .filter {
                                    // TODO should we assert here, seems it is always PsiFileSystemItem
                                    (it.element as? PsiFileSystemItem)?.isDirectory ?: true
                                }
                        )
                    } else {
                        super@CodeownersEntryReference.innerResolveInContext(unescapedText.toString(), context, result, caseSensitive)
                    }
                }
            }

            private fun resolveStrictlyUnderContext(
                text: CharSequence, // can have only leading and middle slashes
                context: PsiFileSystemItem,
                result: MutableCollection<ResolveResult>,
                caseSensitive: Boolean,
                dirOnly: Boolean
            ) {
                // /foo/**/baz
                // check cache
                //   if found - return immediately
                //   otherwise:
                //     split into /foo/** and baz
                //     resolve first part recursively
                //     resolve baz in context of result

                // no escaping handling as slash cannot be part of filename
                // TODO preserve trailing slash in reparse method
                // TODO handle splitting in language implementation
                val (prefix, fragment) = text.splitLast('/')
                when {
                    // no slashes
                    fragment == null -> resolveFragment(prefix, context, result, caseSensitive, dirOnly)
                    // trailing slash is excluded on input text normalization
                    fragment == "" -> TODO("unreachable")
                    prefix == "" -> { // /foo leads to that case, need to resolve fragment as a fragment
                        resolveFragment(fragment, context, result, caseSensitive, dirOnly)
                    }
                    else -> {
                        val contexts = mutableListOf<ResolveResult>()
                        // on recursive call we should resolve strictly in context and to all variants
                        innerResolveInContextRecursive(prefix.toString(), context, contexts, caseSensitive, false, false)
                        contexts.forEach { ctx ->
                            resolveFragment(
                                fragment,
                                ctx.element as? PsiFileSystemItem ?: return@forEach,
                                result,
                                caseSensitive,
                                dirOnly
                            )
                        }
                    }
                }
            }

            private fun resolveAtAnyLevel(
                text: CharSequence, // can have only leading slash
                context: PsiFileSystemItem,
                result: MutableCollection<ResolveResult>,
                caseSensitive: Boolean,
                dirOnly: Boolean
            ) {
                val nameOnly = text.trimStart('/')
                val project = context.project

                val scope = if (dirOnly) {
                    GlobalSearchScopes.filterScope(
                        project,
                        FilteredNamedScope("DirOnly", { "Dir only" }, AllIcons.Nodes.Folder, 0) {
                            it.isDirectory
                        }
                    )
                } else {
                    GlobalSearchScope.allScope(project)
                }

                if (nameOnly.containsMetasymbols()) {
                    val regex = myPatternCache.createRelativePattern(nameOnly, caseSensitive)
                    val names = mutableSetOf<String>()
                    FilenameIndex.processAllFileNames({
                        if (it.matches(regex)) {
                            names.add(it)
                        }
                        true
                    }, scope, null)
                    FilenameIndex.processFilesByNames(names, caseSensitive, scope, null) { file ->
                        getPsiFileSystemItem(psiManager, file)?.let {
                            result.add(
                                PsiElementResolveResult(it)
                            )
                        }
                        true
                    }
                } else {
                    result.addAll(
                        FilenameIndex
                            .getVirtualFilesByName(nameOnly.toString(), caseSensitive, scope)
                            .toResolveResults()
                    )
                }
            }

            // only this method should be called recursively from other methods in this class
            fun innerResolveInContextRecursive(
                normalizedText: CharSequence, // can have only leading and middle slashes, can be empty
                context: PsiFileSystemItem,
                result: MutableCollection<ResolveResult>,
                caseSensitive: Boolean,
                atAnyLevel: Boolean,
                dirOnly: Boolean
            ) {
                ProgressManager.checkCanceled()
                val tracer =
                    tracerStub?.start("CodeownersEntryReference.innerResolveInContextRecursive '$normalizedText' (in $context)")
                withNullableCloseable(tracer) {
                    val cachedResult = myCodeownersPatternsMatchedFilesCache.getFilesByPrefix(normalizedText, atAnyLevel, dirOnly)
                    if (cachedResult.isNotEmpty()) {
                        result.addAll(cachedResult.toResolveResults())
                        return
                    }

                    if (atAnyLevel) {
                        resolveAtAnyLevel(normalizedText, context, result, caseSensitive, dirOnly)
                    } else {
                        resolveStrictlyUnderContext(normalizedText, context, result, caseSensitive, dirOnly)
                    }

                    myCodeownersPatternsMatchedFilesCache.addFilesByPrefix(
                        normalizedText,
                        atAnyLevel,
                        dirOnly,
                        result.mapNotNull { (it.element as? PsiFileSystemItem)?.virtualFile }.toList()
                    )
                }
            }
        }

        /**
         * Resolves reference to the filesystem.
         * Terms:
         * pattern - whole CODEOWNERS file pattern
         * prefix - pattern without one or more trailing fragments
         * fragment - part of the pattern surrounded by slash(es), if any, or the pattern itself
         * BNF grammar:
         * FRAGMENT := regex:[^/]+
         * PREFIX := [/]FRAGMENT | PREFIX/FRAGMENT
         * PATTERN := PREFIX[/[FRAGMENT]]
         *
         * This method SHOULD NOT be invoked recursively from inside Resolver as it performs some calculations
         * meaningful only for calls from outside
         *
         * @param text          file reference path, prefix of the whole pattern
         * @param context       root, text is relative to (CODEOWNERS file dir)
         * @param result        result references collection
         * @param caseSensitive is ignored
         */
        override fun innerResolveInContext(
            text: String,
            context: PsiFileSystemItem,
            result: MutableCollection<ResolveResult>,
            caseSensitive: Boolean
        ) {
            fun CharSequence.ensureStartsWith(char: Char) = if (!this.startsWith(char)) {
                "$char${this}"
            } else {
                this
            }

            ProgressManager.checkCanceled()
            val tracer = tracerStub?.start("CodeownersEntryReference.innerResolveInContext '$text' (in $context)")
            val textSequence = text as CharSequence
            withNullableCloseable(tracer) {
                val (atAnyLevel, dirOnly, normalizedText) = if (index == 0) {
                    // to decide whether we should resolve reference relative to the root or at any level
                    // we need to look at the whole pattern
                    // we cannot rely on the number of references in the set because there may be single reference but
                    // with leading slash
                    val wholePattern = this.fileReferenceSet.pathString
                    // assert: last reference must exist since we have one at hands
//                        this.lastFileReference!!.canonicalText
                    // TODO preserve trailing slash in reparse method
                    var atAnyLevel = when (wholePattern.indexOf('/')) {
                        // no slashes or only trailing one
                        -1, (wholePattern.length - 1) -> true
                        else -> false
                    }
                    // the first ref can be also the last one, so need to check trailing slash
                    // shouldn't remove leading slash to properly cache results
                    var normalizedText = textSequence.trimEnd('/')
                    if (!atAnyLevel) {
                        // for the first ref in set it is meaningful whether it has leading slash or not
                        // should add leading slash if not at any level
                        // to cache foo in /foo/bar and foo/bar under the same key
                        normalizedText = normalizedText.ensureStartsWith('/')
                    }
//                    val dirOnly = normalizedText.length < textSequence.length
                    val dirOnly = wholePattern.endsWith('/')
                    Triple(atAnyLevel, dirOnly, normalizedText)
                } else if (isLast) {
                    val normalizedText = textSequence.trimEnd('/').ensureStartsWith('/')
//                    val dirOnly = normalizedText.length < textSequence.length
                    val wholePattern = this.fileReferenceSet.pathString
                    val dirOnly = wholePattern.endsWith('/')
                    // to cache both /foo/bar and foo/bar under the same key
                    Triple(false, dirOnly, normalizedText)
                } else {
                    // it is not the first ref in the set, so there exists at least one slash, thus we should resolve strictly in context
                    Triple(false, false, textSequence.ensureStartsWith('/'))
                }
                Resolver().innerResolveInContextRecursive(normalizedText, context, result, caseSensitive, atAnyLevel, dirOnly)
            }
            tracer?.toString()?.let(::println)
        }

        /**
         * Searches for directory or file using [PsiManager].
         *
         * @param manager [PsiManager] instance
         * @param file    working file
         * @return Psi item
         */
        private fun getPsiFileSystemItem(manager: PsiManager, file: VirtualFile): PsiFileSystemItem? {
            if (!file.isValid) {
                return null
            }
            return if (file.isDirectory) manager.findDirectory(file) else manager.findFile(file)
        }
    }

    companion object {
        private fun isFileUnderSameVcsRoot(project: Project, vcsRoot: VirtualFile, file: VirtualFile): Boolean {
            val fileVcsRoot = VcsUtil.getVcsRootFor(project, file)
            return fileVcsRoot != null && vcsRoot == fileVcsRoot
        }
    }
}
