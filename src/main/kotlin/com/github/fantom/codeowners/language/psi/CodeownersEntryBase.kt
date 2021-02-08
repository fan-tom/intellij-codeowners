package com.github.fantom.codeowners.language.psi

import com.intellij.psi.PsiElement
import java.util.regex.Pattern

interface CodeownersEntryBase : PsiElement {
    /**
     * Returns current value.
     *
     * @return value
     */
    val value: String

    /**
     * Returns current pattern.
     *
     * @return pattern
     */
    val pattern: Pattern?
}
