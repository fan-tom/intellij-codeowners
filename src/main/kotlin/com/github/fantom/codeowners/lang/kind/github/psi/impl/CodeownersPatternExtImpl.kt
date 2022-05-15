package com.github.fantom.codeowners.lang.kind.github.psi.impl

import com.github.fantom.codeowners.lang.CodeownersElementImpl
import com.github.fantom.codeowners.lang.kind.github.psi.*
import com.intellij.lang.ASTNode

/**
 * Custom [CodeownersElementImpl] implementation.
 */
abstract class CodeownersPatternExtImpl(node: ASTNode) : CodeownersElementImpl(node), CodeownersPattern {
    override val entry: CodeownersEntry
        // either assign or entry is available
        get() = assign?.entryFile ?: reset?.entryFile!!
    override val owners: List<CodeownersOwner>
        // reset matches path and assigns empty owners list
        get() = assign?.owners?.ownerList ?: emptyList()
    override val isExplicitlyUnowned: Boolean
        get() = reset != null
}
