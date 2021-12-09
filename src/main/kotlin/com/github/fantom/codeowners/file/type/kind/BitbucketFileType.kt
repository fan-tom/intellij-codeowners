package com.github.fantom.codeowners.file.type.kind

import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.lang.kind.bitbucket.BitbucketLanguage
import com.intellij.openapi.vcs.VcsRoot
import com.intellij.openapi.vfs.VirtualFile

class BitbucketFileType : CodeownersFileType(BitbucketLanguage.INSTANCE) {
    companion object {
        val INSTANCE = BitbucketFileType()
        private val subdirectories = setOf(".bitbucket")
    }

    private fun isInSubdir(codeownersFile: VirtualFile) = codeownersFile.parent.name in subdirectories

    override fun getRoot(vcsRoot: VcsRoot, codeownersFile: VirtualFile): VirtualFile? {
        return super.getRoot(vcsRoot, codeownersFile) ?: codeownersFile.parent?.let { codeownersParentDir ->
            if (
                codeownersParentDir.parent == vcsRoot.path && //
                codeownersParentDir.name in subdirectories
            ) {
                vcsRoot.path
            } else {
                null
                // find root CODEOWNERS file
//                vcsRoot.findFileByRelativePath(filename)?.let {
//                    PsiManager.getInstance(codeownersFile.project).findFile(it)
//                }?.let {
//                    (it as? BitbucketFile)
//                }?.let { rootCodeownersFile ->
//                    if (rootCodeownersFile.isSubdirectoryOverridesEnabled) {
//                        codeownersParentDir
//                    } else {
//                        throw CodeownersException("Subdirectory overrides are not enabled")
//                    }
//                } ?: throw CodeownersException("No Bitbucket CODEOWNERS file in the root of repository")
            }
        }
    }

    override fun getRoot(codeownersFile: VirtualFile): VirtualFile {
        return if (isInSubdir(codeownersFile)) codeownersFile.parent.parent else super.getRoot(codeownersFile)
    }
}
