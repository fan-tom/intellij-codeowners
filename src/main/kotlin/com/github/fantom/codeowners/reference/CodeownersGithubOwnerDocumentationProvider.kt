package com.github.fantom.codeowners.reference

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement

class CodeownersGithubOwnerDocumentationProvider: AbstractDocumentationProvider() {
    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        return if (element is CodeownersGithubOwnerReference.MyFakePsiElement) {
            element.url
        } else null
    }
}