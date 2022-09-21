package com.github.fantom.codeowners.lang.kind.github

import com.github.fantom.codeowners.OwnersReference
import com.github.fantom.codeowners.file.type.kind.GithubFileType
import com.github.fantom.codeowners.indexing.OwnerString
import com.github.fantom.codeowners.indexing.RegexString
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.github.fantom.codeowners.lang.CodeownersVisitor
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersNamedOwner
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersRule
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersTypes
import com.github.fantom.codeowners.reference.CodeownersGithubOwnerReference
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.tree.IElementType
import com.intellij.util.ProcessingContext
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersVisitor as GithubCodeownersVisitor

class GithubLanguage private constructor() : CodeownersLanguage("Github") {
    companion object {
        val INSTANCE = GithubLanguage()
    }

    override val fileType
        get() = GithubFileType.INSTANCE

    override fun getCrlfToken(): IElementType {
        return CodeownersTypes.CRLF
    }

    override fun getVisitor(visitor: CodeownersVisitor) =
        object : GithubCodeownersVisitor() {
            override fun visitRule(rule: CodeownersRule) {
                visitor.visitRule(rule)
            }
        }

    override fun getPatternsVisitor(items: MutableList<Pair<RegexString, OwnersReference>>) =
        object : GithubCodeownersVisitor() {
            override fun visitRule(rule: CodeownersRule) {
                val regex = rule.pattern.regex(false)
                items.add(
                    Pair(
                        RegexString(regex),
                        OwnersReference(rule.owners.map { OwnerString(it.text) }, rule.textOffset)
                    )
                )
            }
        }

    override fun getReferencesByElement(
        psiElement: PsiElement,
        processingContext: ProcessingContext
    ): Array<out PsiReference>? =
        if (psiElement is CodeownersNamedOwner) {
            arrayOf(CodeownersGithubOwnerReference(psiElement))
        } else {
            super.getReferencesByElement(psiElement, processingContext)
        }
}
