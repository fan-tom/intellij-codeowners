package com.github.fantom.codeowners.lang.kind.bitbucket.psi.impl

import com.github.fantom.codeowners.lang.CodeownersElementImpl
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersOwner
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersPattern
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersRule
import com.intellij.lang.ASTNode

/**
 * Custom [CodeownersElementImpl] implementation.
 */
abstract class CodeownersRuleExtImpl(node: ASTNode) : CodeownersElementImpl(node), CodeownersRule {
    override val pattern: CodeownersPattern
        // either assign or entry is available
        get() = assign?.pattern ?: reset?.pattern!!
    override val owners: List<CodeownersOwner>
        // reset matches path and assigns empty owners list
        get() = assign?.owners?.ownerList ?: emptyList()
    override val isExplicitlyUnowned: Boolean
        get() = reset != null
}
