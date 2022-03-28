package com.github.fantom.codeowners.structureview

import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersTeamDefinition
import com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.psi.PsiFile

class StructureViewModel(psiFile: PsiFile) : StructureViewModelBase(psiFile, StructureViewElement(psiFile)), ElementInfoProvider {
    override fun getSorters(): Array<Sorter> {
        return arrayOf(Sorter.ALPHA_SORTER)
    }

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
        return false
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        return element.value is CodeownersTeamDefinition
    }
}
