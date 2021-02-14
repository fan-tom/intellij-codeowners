package com.github.fantom.codeowners.reference

import com.github.fantom.codeowners.languages.github.psi.CodeownersNamedOwner
import com.intellij.openapi.paths.WebReference
import com.intellij.psi.PsiElement


class CodeownersGithubOwnerReference(val owner: CodeownersNamedOwner) : WebReference(owner, createUrl(owner)) {
    companion object {
        // TODO: extract url into language (and settings for BitBucket/GitLab)
        fun createUrl(owner: CodeownersNamedOwner) = if (owner.ownerName.userName != null)
            "https://github.com/${owner.ownerName.userName!!.text}"
        else
            "https://github.com/orgs/${owner.ownerName.team!!.orgName.text}/teams/${owner.ownerName.team!!.teamName.text}"
    }

    override fun resolve(): MyFakePsiElement? {
        return MyFakePsiElement(super.resolve() ?: return null, createUrl(owner))
    }

    inner class MyFakePsiElement(element: PsiElement, val url: String): PsiElement by element
}