package com.github.fantom.codeowners.structureview

import com.github.fantom.codeowners.lang.CodeownersFile
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersTeamDefinition
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.impl.CodeownersTeamDefinitionImpl
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.util.PsiTreeUtil

class StructureViewElement(private val myElement: NavigatablePsiElement) : StructureViewTreeElement, SortableTreeElement {
    override fun getValue(): Any {
        return myElement
    }

    override fun navigate(requestFocus: Boolean) {
        myElement.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean {
        return myElement.canNavigate()
    }

    override fun canNavigateToSource(): Boolean {
        return myElement.canNavigateToSource()
    }

    override fun getAlphaSortKey(): String {
        val name = myElement.name
        return name ?: ""
    }

    override fun getPresentation(): ItemPresentation {
        val presentation = myElement.presentation
        return presentation ?: PresentationData()
    }

    override fun getChildren(): Array<out TreeElement> {
        if (myElement is CodeownersFile) {
            val teamDefinitions = PsiTreeUtil.getChildrenOfTypeAsList(myElement, CodeownersTeamDefinition::class.java)
            return teamDefinitions.map { StructureViewElement(it as CodeownersTeamDefinitionImpl) }
                .toTypedArray()
        }
        return StructureViewTreeElement.EMPTY_ARRAY
    }
}
