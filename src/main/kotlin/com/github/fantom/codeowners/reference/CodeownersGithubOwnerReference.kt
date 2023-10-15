package com.github.fantom.codeowners.reference

import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersNamedOwner
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.paths.WebReference
import com.intellij.openapi.util.TextRange
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.SyntheticElement
import com.intellij.psi.impl.FakePsiElement

class CodeownersGithubOwnerReference(val owner: CodeownersNamedOwner) : WebReference(owner, createUrl(owner)) {
    companion object {
        // TODO extract url into language (and settings for BitBucket/GitLab)
        fun createUrl(owner: CodeownersNamedOwner) = if (owner.ownerName.userName != null) {
            "https://github.com/${owner.ownerName.userName!!.text}"
        } else {
            "https://github.com/orgs/${owner.ownerName.team!!.orgName.text}/teams/${owner.ownerName.team!!.teamName.text}"
        }
    }

    override fun resolve(): MyFakePsiElement? {
        return MyFakePsiElement(super.resolve() ?: return null, createUrl(owner))
    }

    /**
    * This class basically reimplements [WebReference.MyFakePsiElement] to be able to show
    * custom tooltip on Ctrl-hover
     *
    * WebReference.MyFakePsiElement is handled by [com.intellij.openapi.paths.WebReferenceDocumentationProvider]
    */
    inner class MyFakePsiElement(val element: PsiElement, val url: String) : FakePsiElement(), SyntheticElement {

        override fun getParent(): PsiElement = element.parent

        override fun navigate(requestFocus: Boolean) {
            (element as Navigatable).navigate(requestFocus)
        }

        override fun getPresentableText() = (element as ItemPresentation).presentableText

        override fun getName() = (element as PsiNamedElement).name

        override fun getTextRange(): TextRange? = element.textRange
    }
}
