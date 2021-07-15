package com.github.fantom.codeowners.lang.kind.bitbucket.psi

import com.github.fantom.codeowners.lang.kind.bitbucket.BitbucketLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class CodeownersElementType(@NonNls debugName: String) : IElementType(debugName, BitbucketLanguage.INSTANCE)
