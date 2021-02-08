package com.github.fantom.codeowners.indexing

import com.github.fantom.codeowners.CodeownersFileType
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope

/**
 * Provides extended [GlobalSearchScope] with additional ignore files (i.e. outer gitignore files).
 */
class CodeownersSearchScope private constructor(project: Project) : GlobalSearchScope(project) {

    override fun contains(file: VirtualFile) = file.fileType is CodeownersFileType

    override fun isSearchInLibraries() = true

    override fun isForceSearchingInLibrarySources() = true

    override fun isSearchInModuleContent(aModule: Module) = true

    override fun union(scope: SearchScope) = this

    override fun intersectWith(scope2: SearchScope) = scope2

    companion object {
        /**
         * Returns [GlobalSearchScope.projectScope] instance united with additional files.
         *
         * @param project current project
         * @return extended instance of [GlobalSearchScope]
         */
        operator fun get(project: Project) = CodeownersSearchScope(project)
    }
}
