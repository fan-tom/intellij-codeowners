package com.github.fantom.codeowners.reference

import com.github.fantom.codeowners.language.psi.CodeownersEntry
import com.github.fantom.codeowners.language.psi.CodeownersFile
import com.github.fantom.codeowners.language.psi.CodeownersNamedOwner
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
        override fun getReferencesByElement(psiElement: PsiElement, processingContext: ProcessingContext): Array<out PsiReference> =
                when (psiElement) {
                    is CodeownersEntry -> CodeownersEntryReferenceSet(psiElement).allReferences
                    is CodeownersNamedOwner -> arrayOf(CodeownersGithubOwnerReference(psiElement))
                    else -> PsiReference.EMPTY_ARRAY
                }
    }
}
