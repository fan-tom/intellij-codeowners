package com.github.fantom.codeowners

import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.annotations.NonNls

class CodeownersFileType private constructor() : LanguageFileType(CodeownersLanguage.INSTANCE) {

    companion object {
        val INSTANCE = CodeownersFileType()
    }

    @NonNls
    override fun getName() = "Codeowners File"

    override fun getDescription() = "Codeowners file"

    override fun getDefaultExtension() = "CODEOWNERS"

    override fun getIcon() = CodeownersIcons.FILE

    override fun equals(other: Any?) = other is CodeownersFileType

    override fun hashCode() = INSTANCE.language.id.hashCode()
}
