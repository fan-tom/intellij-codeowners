package com.github.fantom.codeowners.lang

import com.intellij.psi.PsiElement

interface CodeownersPatternBase<EntryType : CodeownersEntryBase, OwnersType> : PsiElement {
    /**
     * Returns fs path.
     */
    val entry: EntryType

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

