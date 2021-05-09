package com.github.fantom.codeowners.lang.kind.bitbucket.psi.impl

import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersElementImpl
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersNamedOwner
import com.intellij.lang.ASTNode

/**
 * Custom [CodeownersElementImpl] implementation.
 */
abstract class CodeownersNamedOwnerExtImpl(node: ASTNode) : CodeownersElementImpl(node), CodeownersNamedOwner
