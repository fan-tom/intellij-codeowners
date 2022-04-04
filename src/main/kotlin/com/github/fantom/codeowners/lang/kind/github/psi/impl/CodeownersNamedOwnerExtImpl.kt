package com.github.fantom.codeowners.lang.kind.github.psi.impl

import com.github.fantom.codeowners.lang.CodeownersElementImpl
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersNamedOwner
import com.intellij.lang.ASTNode

/**
 * Custom [CodeownersElementImpl] implementation.
 */
abstract class CodeownersNamedOwnerExtImpl(node: ASTNode) : CodeownersElementImpl(node), CodeownersNamedOwner
