package com.github.fantom.codeowners.file.type.kind

import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.lang.kind.github.GithubLanguage
import com.intellij.openapi.vcs.VcsRoot
import com.intellij.openapi.vfs.VirtualFile

class GithubFileType : CodeownersFileType(GithubLanguage.INSTANCE) {
    companion object {
        val INSTANCE = GithubFileType()
        private val subdirectories = setOf(".github", "docs")
    }

    // TODO need to investigate why parent may be null
    private fun isInSubdir(codeownersFile: VirtualFile) = codeownersFile.parent?.let { it.name in subdirectories } ?: false

    override fun getRoot(vcsRoot: VcsRoot, codeownersFile: VirtualFile): VirtualFile? {
        return super.getRoot(vcsRoot, codeownersFile) // CODEOWNERS file is allowed in repo root
            ?: codeownersFile.parent?.run {
                name in subdirectories && // and in dirs with given name
                    parent == vcsRoot.path // only if they are in the root
            }?.let { vcsRoot.path }
    }

    override fun getRoot(codeownersFile: VirtualFile): VirtualFile {
        return if (isInSubdir(codeownersFile)) codeownersFile.parent.parent else super.getRoot(codeownersFile)
    }
}
