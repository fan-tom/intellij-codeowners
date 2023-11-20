package com.github.fantom.codeowners.search

import com.intellij.ide.util.scopeChooser.ScopeDescriptor
import com.intellij.ide.util.scopeChooser.ScopeDescriptorProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project

class CodeownersSearchScopeDescriptorProvider : ScopeDescriptorProvider {
    override fun getScopeDescriptors(project: Project, dataContext: DataContext): Array<ScopeDescriptor> {
        return arrayOf(CodeownersSearchScopeDescriptor(project))
    }
}