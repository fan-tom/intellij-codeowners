package com.github.fantom.codeowners.lang.kind.github.psi

import com.github.fantom.codeowners.lang.kind.github.GithubLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class CodeownersTokenType(@NonNls debugName: String) : IElementType(debugName, GithubLanguage.INSTANCE) {
    override fun toString(): String {
        return "CodeownersTokenType." + super.toString()
    }
}
