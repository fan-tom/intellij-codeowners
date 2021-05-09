package com.github.fantom.codeowners.lang

import com.intellij.psi.PsiElement
import java.util.regex.Pattern

interface CodeownersEntryBase : PsiElement {
    /**
     * Returns current value.
     *
     * @return value
     */
    val value: String

    val isDirectory: Boolean

    /**
     * Returns current pattern.
     *
     * @return pattern
     */
    fun regex(acceptChildren: Boolean = false): String

    /**
     * Returns current pattern.
     *
     * @return pattern
     */
    fun pattern(acceptChildren: Boolean = false): Pattern?
}