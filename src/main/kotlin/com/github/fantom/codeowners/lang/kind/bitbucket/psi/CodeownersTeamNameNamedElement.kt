package com.github.fantom.codeowners.lang.kind.bitbucket.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil

interface CodeownersTeamNameNamedElement : PsiNameIdentifierOwner {
    val teamName: CodeownersTeamName
}

// class CodeownersTeamNameNamedElementFactory

abstract class CodeownersTeamNameNamedElementImpl(node: ASTNode) :
    ASTWrapperPsiElement(node),
    CodeownersTeamNameNamedElement {
    companion object {
        private val LOGGER = Logger.getInstance(CodeownersTeamReference::class.java)
    }
    override fun getName(): String? {
        return teamName.text.also {
            LOGGER.trace("> getName: $it")
        }
    }

    override fun setName(newName: String): PsiElement? {
//        val teamNameNode = node.findChildByType(CodeownersTypes.TEAM_NAME)
//        if (teamNameNode != null) {
//            val property = SimpleElementFactory.createProperty(project, newName)
//            val newTeamNameNode = property.getFirstChild().getNode()
//            node.replaceChild(teamNameNode, newTeamNameNode)
//        }
        return this
    }

    override fun getNameIdentifier() = node.findChildByType(CodeownersTypes.TEAM_NAME)?.psi.also {
        LOGGER.trace("< getNameIdentifier: ${it?.text}")
    }
}

class CodeownersTeamReference(element: CodeownersTeam) :
    PsiReferenceBase<PsiElement>(
        element,
        element.teamName.textRangeInParent.also {
            LOGGER.trace("> textRange: $it")
        }
    ),
    PsiReference {
    companion object {
        private val LOGGER = Logger.getInstance(CodeownersTeamReference::class.java)
    }
    override fun resolve(): PsiElement? {
        LOGGER.trace("> resolve: $this")
        val teamDefinitions =
            PsiTreeUtil.getChildrenOfType(element.containingFile, CodeownersTeamDefinition::class.java) ?: run {
                LOGGER.trace("> resolved to null")
                return null
            }
        return teamDefinitions.firstOrNull { it.teamName.name_.text == (element as CodeownersTeam).teamName.text }.also {
            LOGGER.trace("> resolve: $it")
        }
    }
}
