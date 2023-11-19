package com.github.fantom.codeowners.search

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.CodeownersIcons
import com.github.fantom.codeowners.CodeownersManager
import com.github.fantom.codeowners.search.ui.CodeownersSearchFilterDialog
import com.intellij.ide.util.scopeChooser.ScopeDescriptor
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope

class CodeownersSearchScopeDescriptor(private val project: Project) : ScopeDescriptor(null) {
    private val manager = project.service<CodeownersManager>()

    private var cachedScope: SearchScope? = null

    override fun getDisplayName(): String {
        return CodeownersBundle.message("search.scope.name")
    }

    override fun getIcon() = CodeownersIcons.FILE

    override fun scopeEquals(scope: SearchScope?): Boolean {
        return cachedScope == scope
    }

    override fun getScope(): SearchScope? {
        if (cachedScope == null) {
            val codeownersFiles = manager.getCodeownersFiles().ifEmpty { return null }

            val dialog = CodeownersSearchFilterDialog(project, codeownersFiles)

            if (!dialog.showAndGet()) {
                cachedScope = GlobalSearchScope.EMPTY_SCOPE
                return null
            }

            val result = dialog.result!! // it cannot be null

            val (codeownersFile, dnf) = result

            cachedScope = CodeownersSearchScope(project, CodeownersSearchFilter(codeownersFile, DNF(dnf)))
        }

        return cachedScope
    }
}