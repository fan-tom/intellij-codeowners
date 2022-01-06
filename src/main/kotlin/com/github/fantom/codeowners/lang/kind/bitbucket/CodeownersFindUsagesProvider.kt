package com.github.fantom.codeowners.lang.kind.bitbucket

import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersTeamDefinition
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersTypes
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

class CodeownersFindUsagesProvider : FindUsagesProvider {
    companion object {
        private val LOGGER = Logger.getInstance(CodeownersFindUsagesProvider::class.java)
    }
    override fun getWordsScanner(): WordsScanner {
        return DefaultWordsScanner(
            CodeownersLexerAdapter(),
            TokenSet.create(CodeownersTypes.NAME_),
            TokenSet.create(CodeownersTypes.COMMENT),
            TokenSet.EMPTY
        )
    }

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        LOGGER.trace("> canFindUsagesFor $psiElement")
        return psiElement is CodeownersTeamDefinition
    }

    override fun getHelpId(psiElement: PsiElement): String? {
        return null
    }

    override fun getType(element: PsiElement): String {
        return "team"
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return (element as? CodeownersTeamDefinition)?.teamName?.text ?: ""
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return (element as? CodeownersTeamDefinition)?.text ?: ""
    }
}
