package com.github.fantom.codeowners.lang

import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class CodeownersTokenType(@NonNls debugName: String) : IElementType(debugName, CodeownersLanguage.INSTANCE) {
    override fun toString(): String {
        return "CodeownersTokenType." + super.toString()
    }
}
