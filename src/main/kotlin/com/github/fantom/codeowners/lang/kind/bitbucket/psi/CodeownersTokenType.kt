package com.github.fantom.codeowners.lang.kind.bitbucket.psi

import com.github.fantom.codeowners.lang.kind.bitbucket.BitbucketLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class CodeownersTokenType(@NonNls debugName: String) : IElementType(debugName, BitbucketLanguage.INSTANCE) {
    override fun toString(): String {
        return "CodeownersTokenType." + super.toString()
    }
}
