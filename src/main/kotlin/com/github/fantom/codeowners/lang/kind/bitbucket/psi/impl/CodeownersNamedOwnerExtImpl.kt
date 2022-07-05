package com.github.fantom.codeowners.lang.kind.bitbucket.psi.impl

import com.github.fantom.codeowners.lang.CodeownersElementImpl
import com.intellij.lang.ASTNode

/**
 * Custom [CodeownersElementImpl] implementation.
 */
@Suppress("UnnecessaryAbstractClass")
abstract class CodeownersNamedOwnerExtImpl(node: ASTNode) : CodeownersElementImpl(node)
