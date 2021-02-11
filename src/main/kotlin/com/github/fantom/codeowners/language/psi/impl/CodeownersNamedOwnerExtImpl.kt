package com.github.fantom.codeowners.language.psi.impl

import com.github.fantom.codeowners.language.psi.*
import com.intellij.lang.ASTNode

/**
 * Custom [CodeownersElementImpl] implementation.
 */
abstract class CodeownersNamedOwnerExtImpl(node: ASTNode) : CodeownersElementImpl(node), CodeownersNamedOwner
