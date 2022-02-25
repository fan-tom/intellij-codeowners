package com.github.fantom.codeowners.reference

import com.github.fantom.codeowners.lang.CodeownersFile
import com.github.fantom.codeowners.services.CodeownersMatcher
import com.github.fantom.codeowners.util.Constants
import com.github.fantom.codeowners.util.Glob
import com.github.fantom.codeowners.util.MatcherUtil
import com.github.fantom.codeowners.util.Utils
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import com.intellij.util.containers.ContainerUtil
import com.jetbrains.rd.util.concurrentMapOf

/**
 * [FileReferenceSet] definition class.
 */
class CodeownersEntryReferenceSet(element: PsiElement) : FileReferenceSet(element) {

    private val matcher = element.project.service<CodeownersMatcher>()

    override fun createFileReference(range: TextRange, index: Int, text: String) =
        CodeownersEntryReference(this, range, index, text)

    override fun isEndingSlashNotAllowed() = false

    override fun computeDefaultContexts() =
        element.containingFile.parent?.let(::listOf) ?: super.computeDefaultContexts()

    override fun getLastReference() = super.getLastReference()?.let {
        when {
            it.canonicalText.endsWith(separatorString) && myReferences != null && myReferences.size > 1 ->
                myReferences[myReferences.size - 2]
            else -> null
        }
    }

    override fun couldBeConvertedTo(relative: Boolean) = false

    @Suppress("ComplexMethod")
    override fun reparse() {
        ProgressManager.checkCanceled()
        val str = StringUtil.trimEnd(pathString, separatorString)
        val referencesList: MutableList<FileReference?> = ArrayList()
        val separatorString = separatorString // separator's length can be more then 1 char
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
            val fileReference = createFileReference(TextRange(startInElement, startInElement + sepLen), index++, separatorString)
            referencesList.add(fileReference)
        }
        while (true) {
            ProgressManager.checkCanceled()
            val nextSlash = str.indexOf(separatorString, currentSlash + sepLen)
            val subReferenceText = if (nextSlash > 0) str.substring(0, nextSlash) else str
            val range = TextRange(
                startInElement + currentSlash + sepLen,
                startInElement +
                    if (nextSlash > 0) nextSlash else str.length
            )
            val ref = createFileReference(range, index++, subReferenceText)
            referencesList.add(ref)
            if (nextSlash.also { currentSlash = it } < 0) {
                break
            }
        }
        myReferences = referencesList.toTypedArray()
    }

    inner class CodeownersEntryReference(
        fileReferenceSet: FileReferenceSet,
        range: TextRange?,
        index: Int,
        text: String?
    ) :
        FileReference(fileReferenceSet, range, index, text) {
        private val cacheMap = concurrentMapOf<String, Collection<VirtualFile>>()

        @Suppress("ComplexMethod", "NestedBlockDepth", "ReturnCount", "LongMethod")
        override fun innerResolveInContext(
            text: String,
            context: PsiFileSystemItem,
            result: MutableCollection<ResolveResult>,
            caseSensitive: Boolean,
        ) {
            ProgressManager.checkCanceled()
            super.innerResolveInContext(text, context, result, caseSensitive)
            val codeownersFile = containingFile as? CodeownersFile ?: return
            val contextVirtualFile = when {
                Utils.isInProject(codeownersFile.virtualFile, element.project) -> {
                    context.virtualFile
                }
                else -> return
            }
            if (contextVirtualFile != null) {
                val entry =
                    fileReferenceSet.element // as com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry
                val current = canonicalText
                val pattern =
                    Glob.createPattern(current, acceptChildren = false, supportSquareBrackets = false) ?: return
                // TODO think about how to make it more precise. We need to know vcs root to resolve root dir properly
                val root = element.containingFile.virtualFile?.let { (element.containingFile as CodeownersFile).fileType.getRoot(it) }
                val psiManager = element.manager

                ContainerUtil.createConcurrentList<VirtualFile>().run {
                    addAll(MatcherUtil.getFilesForPattern(context.project, pattern))
                    if (isEmpty()) {
                        addAll(context.virtualFile.children)
                    } else if (current.endsWith(Constants.STAR) && current != entry.text) {
                        addAll(context.virtualFile.children.filter { it.isDirectory })
                    } else if (current.endsWith(Constants.DOUBLESTAR)) {

                        val children = cacheMap.getOrPut(entry.text) {
                            val children = mutableListOf<VirtualFile>()
                            val visitor = object : VirtualFileVisitor<Any>() {
                                override fun visitFile(file: VirtualFile) =
                                    file.isDirectory.also {
                                        if (it) {
                                            children.add(file)
                                        }
                                    }
                            }

                            filter(VirtualFile::isDirectory).forEach {
                                ProgressManager.checkCanceled()
                                VfsUtil.visitChildrenRecursively(it, visitor)
                                children.remove(it)
                            }

                            children
                        }
                        clear()
                        addAll(children)
                    }

                    forEach { file ->
                        ProgressManager.checkCanceled()
//                        if (Utils.isVcsDirectory(file)) {
//                            return@forEach
//                        }
                        val name = if (root != null) Utils.getRelativePath(root, file) else file.name
                        if (matcher.match(pattern, name)) {
                            val psiFileSystemItem = getPsiFileSystemItem(psiManager, file) ?: return@forEach
                            result.add(PsiElementResolveResult(psiFileSystemItem))
                        }
                    }
                }
            }
        }

        private fun getPsiFileSystemItem(manager: PsiManager, file: VirtualFile): PsiFileSystemItem? {
            if (!file.isValid) {
                return null
            }
            return manager.findDirectory(file).takeIf { file.isDirectory } ?: manager.findFile(file)
        }
    }
}
