package com.github.fantom.codeowners.lang

import com.intellij.psi.PsiElement

interface CodeownersRuleBase<PatternType : CodeownersPatternBase, OwnersType : PsiElement> : PsiElement {
    /**
     * Returns fs path.
     */
    val pattern: PatternType

    /**
     * Returns non-empty list if this pattern assigns owners
     * and empty list if it resets them (negation case)
     */
    val owners: List<OwnersType>

    /**
     * Some CODEOWNERS implementations allow to unset any early assigned ownership for given file path
     */
    val isExplicitlyUnowned: Boolean
}
