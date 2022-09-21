package com.github.fantom.codeowners.lang.kind.github.psi.impl

import com.github.fantom.codeowners.lang.CodeownersElementImpl
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersPattern
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersPatternDirectory
import com.github.fantom.codeowners.util.Glob
import com.intellij.lang.ASTNode

/**
 * Custom [CodeownersElementImpl] implementation.
 */
abstract class CodeownersPatternExtImpl(node: ASTNode) : CodeownersElementImpl(node), CodeownersPattern {

    /**
     * Checks if current entry is a directory - i.e. `dir/`.
     *
     * @return is directory
     */
    override val isDirectory
        get() = this is CodeownersPatternDirectory

    /**
     * Returns entry value
     *
     * @return entry value
     */
    override val value: String
        get() = text

    /**
     * Returns entries pattern.
     *
     * @return pattern
     */
    override fun regex(acceptChildren: Boolean) = Glob.createRegex(value, acceptChildren, supportSquareBrackets = false)

    /**
     * Returns entries pattern.
     *
     * @return pattern
     */
    override fun pattern(acceptChildren: Boolean) = Glob.createPattern(value, acceptChildren, supportSquareBrackets = false)
}
