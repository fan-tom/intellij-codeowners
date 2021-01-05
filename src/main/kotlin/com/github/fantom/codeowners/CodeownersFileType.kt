package com.github.fantom.codeowners

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

internal class CodeownersFileType private constructor() : LanguageFileType(CodeownersLanguage.INSTANCE) {
    override fun getName(): String {
        return "Codeowners File"
    }

    override fun getDescription(): String {
        return "Codeowners file"
    }

    override fun getDefaultExtension(): String {
        return "CODEOWNERS"
    }

    override fun getIcon(): Icon? {
        return CodeownersIcons.FILE
    }

    companion object {
        val INSTANCE = CodeownersFileType()
    }
}
