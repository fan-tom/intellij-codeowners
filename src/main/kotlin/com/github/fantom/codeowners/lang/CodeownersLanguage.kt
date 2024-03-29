package com.github.fantom.codeowners.lang

import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.reference.CodeownersPatternReferenceSetRecursiveReverse
import com.github.fantom.codeowners.util.TimeTracerKey
import com.github.fantom.codeowners.util.withNullableCloseable
import com.intellij.lang.Language
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.tree.IElementType
import com.intellij.util.ProcessingContext

/**
 * Codeowners [Language] definition
 * We pass CODEOWNERS as a base language to implement default common functionality for all codeowners languages
 */
open class CodeownersLanguage protected constructor(name: String) : Language(INSTANCE, name) {
    val filename = "CODEOWNERS"

    override fun getDisplayName() = "$filename ($id)"

    constructor() : this("CODEOWNERS")

    open val fileType
        get() = CodeownersFileType.INSTANCE

    companion object {
        val INSTANCE: CodeownersLanguage = CodeownersLanguage()
    }

    fun createFile(viewProvider: FileViewProvider) = CodeownersFile(viewProvider, fileType)

    // TODO tech debt, find a way to have abstract/reused token types
    open fun getCrlfToken(): IElementType = TODO("This method should be overridden in class ${this::class}")

    // TODO check if we can simply throw exception from this method to not make return type nullable
    open fun getVisitor(visitor: CodeownersVisitor): PsiElementVisitor? = null
    //    open fun getPatternsVisitor(items: MutableList<Pair<RegexString, OwnersReference>>): PsiElementVisitor? = null
    open fun getReferencesByElement(
        psiElement: PsiElement,
        processingContext: ProcessingContext
    ): Array<out PsiReference>? =
        processingContext.get(TimeTracerKey).let {
            it?.start()
            withNullableCloseable(it) {
                when (psiElement) {
                    is CodeownersPatternBase -> CodeownersPatternReferenceSetRecursiveReverse(psiElement, it?.nested()).allReferences
                    else -> null
                }
            }
        }
}
