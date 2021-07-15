package com.github.fantom.codeowners.lang.kind.bitbucket

import com.github.fantom.codeowners.OwnersReference
import com.github.fantom.codeowners.file.type.kind.BitbucketFileType
import com.github.fantom.codeowners.indexing.OwnerString
import com.github.fantom.codeowners.indexing.PatternString
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersPattern
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersVisitor

class BitbucketLanguage : CodeownersLanguage("Bitbucket", ".bitbucket") {
    companion object {
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
}
