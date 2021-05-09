package com.github.fantom.codeowners.lang.kind.bitbucket.psi

import com.intellij.psi.PsiElement

interface CodeownersPatternBase : PsiElement {
    /**
     * Returns fs path.
     */
    val entry: CodeownersEntry
    val owners: List<CodeownersOwner>
}
