package com.github.fantom.codeowners.lang.kind.bitbucket

import com.github.fantom.codeowners.file.type.kind.BitbucketFileType
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.github.fantom.codeowners.lang.CodeownersVisitor
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersRule
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersTeam
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersTeamReference
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersTypes
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.tree.IElementType
import com.intellij.util.ProcessingContext
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersVisitor as BitbucketCodeownersVisitor

class BitbucketLanguage private constructor() : CodeownersLanguage("Bitbucket") {
    companion object {
        private val LOGGER = Logger.getInstance(BitbucketLanguage::class.java)
        val INSTANCE = BitbucketLanguage()
    }

    override val fileType
        get() = BitbucketFileType

    override fun getCrlfToken(): IElementType {
        return CodeownersTypes.CRLF
    }

    override fun getVisitor(visitor: CodeownersVisitor) =
        object : BitbucketCodeownersVisitor() {
            override fun visitRule(rule: CodeownersRule) {
                visitor.visitRule(rule)
            }
        }

//    override fun getPatternsVisitor(items: MutableList<Pair<RegexString, OwnersReference>>) =
//        object : BitbucketCodeownersVisitor() {
//            override fun visitRule(rule: CodeownersRule) {
//                val regex = rule.pattern.regex(false)
//                items.add(
//                    Pair(
//                        RegexString(regex),
//                        OwnersReference(rule.owners.map { OwnerString(it.text) }, rule.textOffset)
//                    )
//                )
//            }
//        }

    override fun getReferencesByElement(
        psiElement: PsiElement,
        processingContext: ProcessingContext
    ): Array<out PsiReference>? {
        LOGGER.trace("> getReferencesByElement bb for $psiElement")
        return if (psiElement is CodeownersTeam) {
            arrayOf(CodeownersTeamReference(psiElement))
        } else {
            super.getReferencesByElement(psiElement, processingContext)
        }.also {
            LOGGER.trace("< getReferencesByElement bb: $it")
        }
    }
}
