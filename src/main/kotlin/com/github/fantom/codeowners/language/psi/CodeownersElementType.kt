package com.github.fantom.codeowners.language.psi

import com.intellij.psi.tree.IElementType
import com.github.fantom.codeowners.CodeownersLanguage
import org.jetbrains.annotations.NonNls

class CodeownersElementType(@NonNls debugName: String) : IElementType(debugName, CodeownersLanguage.INSTANCE)
