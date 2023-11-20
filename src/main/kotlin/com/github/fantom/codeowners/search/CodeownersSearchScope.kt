package com.github.fantom.codeowners.search

import com.github.fantom.codeowners.CodeownersIcons
import com.github.fantom.codeowners.CodeownersManager
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope


class CodeownersSearchScope(
    project: Project,
    private val filterCtx: CodeownersSearchFilter
): GlobalSearchScope(project) {
    private val manager = project.service<CodeownersManager>()

    override fun contains(file: VirtualFile): Boolean {
        with(manager) {
            return filterCtx.satisfies(file)
        }
    }

    override fun isSearchInModuleContent(aModule: Module) = true

    override fun isSearchInLibraries() = true

    override fun getDisplayName() = filterCtx.displayName()

    override fun getIcon() = CodeownersIcons.FILE
}
