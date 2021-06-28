package com.github.fantom.codeowners.lang.kind.github

import com.github.fantom.codeowners.OwnersReference
import com.github.fantom.codeowners.file.type.kind.GithubFileType
import com.github.fantom.codeowners.indexing.OwnerString
import com.github.fantom.codeowners.indexing.PatternString
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersNamedOwner
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersPattern
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersVisitor
import com.github.fantom.codeowners.reference.CodeownersEntryReferenceSet
import com.github.fantom.codeowners.reference.CodeownersGithubOwnerReference
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext

class GithubLanguage : CodeownersLanguage("Github", ".github") {
    companion object {
        val INSTANCE = GithubLanguage()
    }

    override val fileType
        get() = GithubFileType.INSTANCE

    override fun getPatternsVisitor(items: MutableList<Pair<PatternString, OwnersReference>>) =
        object : CodeownersVisitor() {
            override fun visitPattern(entry: CodeownersPattern) {
                val regex = entry.entryFile.regex(false)
                items.add(
                    Pair(
                        PatternString(regex),
                        OwnersReference(entry.owners.ownerList.map { OwnerString(it.text) }, entry.textOffset)
                    )
                )
            }
        }

    override fun getReferencesByElement(psiElement: PsiElement, processingContext: ProcessingContext): Array<out PsiReference>?  =
            when (psiElement) {
                is CodeownersEntry -> CodeownersEntryReferenceSet(psiElement).allReferences
                is CodeownersNamedOwner -> arrayOf(CodeownersGithubOwnerReference(psiElement))
                else -> null
            }

}
