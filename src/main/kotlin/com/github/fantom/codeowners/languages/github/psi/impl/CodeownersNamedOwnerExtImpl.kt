package com.github.fantom.codeowners.languages.github.psi.impl

import com.github.fantom.codeowners.languages.github.psi.CodeownersElementImpl
import com.github.fantom.codeowners.languages.github.psi.*
import com.intellij.lang.ASTNode

/**
 * Custom [CodeownersElementImpl] implementation.
 */
abstract class CodeownersNamedOwnerExtImpl(node: ASTNode) : CodeownersElementImpl(node), CodeownersNamedOwner
