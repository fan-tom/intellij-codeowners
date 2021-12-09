package com.github.fantom.codeowners.file.type

import com.github.fantom.codeowners.CodeownersIcons
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.vcs.VcsRoot
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NonNls

open class CodeownersFileType protected constructor(
    val codeownersLanguage: CodeownersLanguage = CodeownersLanguage.INSTANCE
) : LanguageFileType(codeownersLanguage) {

    companion object {
        val INSTANCE = CodeownersFileType()
    }

    /**
     * Return directory we should treat as prefix for all the entries in given [codeownersFile]
     * relatively to given [vcsRoot].
     * @return null if paths in given [codeownersFile] cannot be resolved relatively to given [vcsRoot], i.e.
     * provided vcsRoot doesn't contain this CODEONWERS file,
     * or if file is not in one of allowed places under repository root
     */
    open fun getRoot(vcsRoot: VcsRoot, codeownersFile: VirtualFile): VirtualFile? {
        return if (vcsRoot.path == codeownersFile.parent) vcsRoot.path else null
    }

    /**
     * @return directory we should treat as prefix for all the entries in given [codeownersFile]
     * This method is imprecise and should be avoided if possible, consider using [getRoot] that takes [VcsRoot]
     */
    open fun getRoot(codeownersFile: VirtualFile): VirtualFile {
        return codeownersFile.parent
    }

    @NonNls
    override fun getName() = "${codeownersLanguage.id} File"

    val languageName
        get() = codeownersLanguage.id

    override fun getDescription() = codeownersLanguage.displayName // "Codeowners file"

    override fun getDefaultExtension() = "CODEOWNERS"

    override fun getIcon() = CodeownersIcons.FILE

    override fun equals(other: Any?) = other is CodeownersFileType && languageName == other.languageName

    override fun hashCode() = codeownersLanguage.id.hashCode()

    override fun toString() = name
}
