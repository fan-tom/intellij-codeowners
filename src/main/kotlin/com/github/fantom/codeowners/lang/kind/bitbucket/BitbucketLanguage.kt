package com.github.fantom.codeowners.lang.kind.bitbucket

import com.github.fantom.codeowners.OwnersReference
import com.github.fantom.codeowners.file.type.kind.BitbucketFileType
import com.github.fantom.codeowners.indexing.OwnerString
import com.github.fantom.codeowners.indexing.PatternString
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersPattern
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersTeam
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersTeamReference
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersVisitor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext

class BitbucketLanguage private constructor() : CodeownersLanguage("Bitbucket") {
    companion object {
        private val LOGGER = Logger.getInstance(BitbucketLanguage::class.java)
        val INSTANCE = BitbucketLanguage()
    }

    override val fileType
        get() = BitbucketFileType.INSTANCE

    override fun getPatternsVisitor(items: MutableList<Pair<PatternString, OwnersReference>>) =
        object : CodeownersVisitor() {
            override fun visitPattern(entry: CodeownersPattern) {
                val regex = entry.entry.regex(false)
                items.add(
                    Pair(
                        PatternString(regex),
                        OwnersReference(entry.owners.map { OwnerString(it.text) }, entry.textOffset)
                    )
                )
            }
        }

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
