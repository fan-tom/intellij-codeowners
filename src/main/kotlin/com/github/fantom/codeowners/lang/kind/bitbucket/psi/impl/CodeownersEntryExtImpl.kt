package com.github.fantom.codeowners.lang.kind.bitbucket.psi.impl

import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersElementImpl
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersEntry
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersEntryDirectory
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersNegation
import com.github.fantom.codeowners.util.Glob
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.text.StringUtil


/**
 * Custom [CodeownersElementImpl] implementation.
 */
abstract class CodeownersEntryExtImpl(node: ASTNode) : CodeownersElementImpl(node), CodeownersEntry {

    /**
     * Checks if the first child is negated - i.e. `!file.txt` entry.
     *
     * @return first child is negated
     */
    val isNegated
        get() = firstChild is CodeownersNegation
    /**
     * Checks if current entry is a directory - i.e. `dir/`.
     *
     * @return is directory
     */
    override val isDirectory
        get() = this is CodeownersEntryDirectory

    /**
     * Returns entry value without leading `!` if entry is negated.
     *
     * @return entry value without `!` negation sign
     */
    override val value
        get() = text.takeIf { !isNegated } ?: StringUtil.trimStart(text, "!")

    /**
     * Returns entries pattern.
     *
     * @return pattern
     */
    override fun regex(acceptChildren: Boolean) = Glob.createRegex(value, acceptChildren, true)

    /**
     * Returns entries pattern.
     *
     * @return pattern
     */
    override fun pattern(acceptChildren: Boolean) = Glob.createPattern(value, acceptChildren, true)
}
