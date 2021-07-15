package com.github.fantom.codeowners.file.type

import com.github.fantom.codeowners.CodeownersIcons
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NonNls

open class CodeownersFileType protected constructor(
    val codeownersLanguage: CodeownersLanguage = CodeownersLanguage.INSTANCE
) : LanguageFileType(codeownersLanguage), FileTypeIdentifiableByVirtualFile {

    companion object {
        val INSTANCE = CodeownersFileType()
    }

    override fun isMyFileType(file: VirtualFile): Boolean {
        val res =
            file.nameSequence == codeownersLanguage.filename &&
                file.parent.name == codeownersLanguage.directory
//        LOGGER.trace("Detected ${codeownersLanguage.id} lang: $res")
        return res
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
