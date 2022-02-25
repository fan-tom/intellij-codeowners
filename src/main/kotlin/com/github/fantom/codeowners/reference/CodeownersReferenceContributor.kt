package com.github.fantom.codeowners.reference

import com.github.fantom.codeowners.lang.CodeownersFile
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext

/**
 * PSI elements references contributor.
 */
class CodeownersReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(psiReferenceRegistrar: PsiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement().inFile(PlatformPatterns.psiFile(CodeownersFile::class.java)),
            CodeownersReferenceProvider()
        )
    }

    private class CodeownersReferenceProvider : PsiReferenceProvider() {
        override fun getReferencesByElement(
            psiElement: PsiElement,
            processingContext: ProcessingContext
        ): Array<out PsiReference> {
            println("> getReferencesByElement for $psiElement")
            return (psiElement.language as? CodeownersLanguage)?.getReferencesByElement(psiElement, processingContext)
                ?: PsiReference.EMPTY_ARRAY
//        when (psiElement) {
//            is com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry ->
            //            CodeownersEntryReferenceSet(psiElement).allReferences
//            is com.github.fantom.codeowners.lang.kind.github.psi.CodeownersNamedOwner ->
            //            arrayOf(CodeownersGithubOwnerReference(psiElement))
//            else -> PsiReference.EMPTY_ARRAY
//        }
        }
    }
}
