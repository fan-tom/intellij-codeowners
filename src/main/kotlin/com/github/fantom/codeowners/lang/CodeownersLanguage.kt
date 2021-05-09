package com.github.fantom.codeowners.lang

import com.github.fantom.codeowners.OwnersReference
import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.indexing.OwnerString
import com.github.fantom.codeowners.indexing.PatternString
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry
import com.github.fantom.codeowners.reference.CodeownersEntryReferenceSet
import com.intellij.lang.Language
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext

/**
 * Codeowners [Language] definition
 */
open class CodeownersLanguage protected constructor(
        name: String,
        val directory: String? = null,
): Language(name) {
    val filename = "CODEOWNERS"

    override fun getDisplayName() = "$filename ($id)"

    constructor() : this("CODEOWNERS")

    open val fileType
        get() = CodeownersFileType.INSTANCE

    companion object {
        val INSTANCE: CodeownersLanguage = CodeownersLanguage()
    }

    fun createFile(viewProvider: FileViewProvider) = CodeownersFile(viewProvider, fileType)

    open fun getPatternsVisitor(items: MutableList<Pair<PatternString, OwnersReference>>): PsiElementVisitor? = null
    open fun getReferencesByElement(psiElement: PsiElement, processingContext: ProcessingContext): Array<out PsiReference>? =
            when (psiElement) {
                is CodeownersEntryBase -> CodeownersEntryReferenceSet(psiElement).allReferences
                else -> null
            }
}