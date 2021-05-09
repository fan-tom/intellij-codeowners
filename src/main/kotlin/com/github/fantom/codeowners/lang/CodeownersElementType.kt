package com.github.fantom.codeowners.lang

import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class CodeownersElementType(@NonNls debugName: String) : IElementType(debugName, CodeownersLanguage.INSTANCE)
