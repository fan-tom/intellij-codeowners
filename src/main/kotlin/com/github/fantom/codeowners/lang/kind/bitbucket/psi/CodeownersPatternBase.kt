package com.github.fantom.codeowners.lang.kind.bitbucket.psi

import com.intellij.psi.PsiElement

interface CodeownersPatternBase : PsiElement {
    /**
     * Returns fs path.
     */
    val entry: CodeownersEntry

    /**
     * Returns non-empty list if this pattern assigns owners
     * and empty list if it resets them (negation case)
     */
    val owners: List<CodeownersOwner>
}
