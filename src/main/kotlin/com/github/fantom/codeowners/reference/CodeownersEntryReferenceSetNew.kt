package com.github.fantom.codeowners.reference

import com.github.fantom.codeowners.lang.CodeownersEntryBase
import com.github.fantom.codeowners.lang.CodeownersFile
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.github.fantom.codeowners.services.PatternCache
import com.github.fantom.codeowners.util.TimeTracerStub
import com.github.fantom.codeowners.util.withNullableCloseable
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vcs.changes.ignore.util.RegexUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiManager
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import com.intellij.vcsUtil.VcsUtil

class CodeownersEntryReferenceSetNew(element: CodeownersEntryBase, val tracerStub: TimeTracerStub? = null) :
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
            val referencesList: MutableList<FileReference?> = mutableListOf()
            val separatorString = separatorString // separator's length can be more than 1 char
            val sepLen = separatorString.length
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
        /**
         * Resolves reference to the filesystem.
         * @param text          entry
         * @param context       filesystem context
         * @param result        result references collection
         * @param caseSensitive is ignored
         *
         * copied from com.intellij.openapi.vcs.changes.ignore.reference.IgnoreReferenceSet.IgnoreReference
         */
        override fun innerResolveInContext(
            text: String,
            context: PsiFileSystemItem,
            result: MutableCollection<ResolveResult>,
            caseSensitive: Boolean
        ) {
            ProgressManager.checkCanceled()
            val tracer = tracerStub?.start("CodeownersEntryReference.innerResolveInContext '$text' (in $context)")
            withNullableCloseable(tracer) {
                super.innerResolveInContext(text, context, result, caseSensitive)
                val containingFile = containingFile as? CodeownersFile ?: return
                val codeownersFileAffectedRoot = containingFile.language.fileType.getRoot(containingFile.virtualFile)
                tracer?.log("getRoot")
                val codeownersFileVcsRoot = VcsUtil.getVcsRootFor(context.project, codeownersFileAffectedRoot) ?: return
                val contextVirtualFile = context.virtualFile
                if (contextVirtualFile != null) {
                    val current = canonicalText
                    val pattern = myPatternCache.createPattern(current)
                    if (pattern != null) {
                        val parent = element.containingFile.parent
                        val root = parent?.virtualFile
                        val psiManager = element.manager
                        tracer?.log("getVcsRootFor($codeownersFileAffectedRoot)")
                        val files = myCodeownersPatternsMatchedFilesCache.getFilesForPattern(pattern).toMutableList()
                        tracer?.log("getFilesForPattern($pattern) = ${files.size}")
                        if (files.isEmpty()) {
                            files.addAll(
                                context.virtualFile.children.filter { virtualFile: VirtualFile ->
                                    isFileUnderSameVcsRoot(context.project, codeownersFileVcsRoot, virtualFile)
                                }
                            )
                            tracer?.log("addAll")
                        }
//                    if (pattern.toString() == "^server/general/Users/src/(?:[^/]*/)*?java/projects/pt/?\$") {
//                        Exception().printStackTrace()
//                    }
                        for (file in files) {
                            ProgressManager.checkCanceled()
                            if (!isFileUnderSameVcsRoot(context.project, codeownersFileVcsRoot, file)) {
                                continue
                            }
                            val name = if (root != null)
                                VfsUtilCore.getRelativePath(file, root)
                            else
                                VfsUtilCore.getRelativePath(file, codeownersFileVcsRoot) ?: file.name
                            if (RegexUtil.match(pattern, name)) {
                                getPsiFileSystemItem(psiManager, file)?.also {
                                    result.add(PsiElementResolveResult(it))
                                }
                            }
                        }
                        tracer?.log("For loop")
                    }
                }
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
