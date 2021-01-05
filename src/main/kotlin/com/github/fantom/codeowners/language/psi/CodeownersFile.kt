package com.github.fantom.codeowners.language.psi

import com.intellij.psi.FileViewProvider
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.github.fantom.codeowners.CodeownersLanguage
import com.github.fantom.codeowners.CodeownersFileType

class CodeownersFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, CodeownersLanguage.INSTANCE) {
    override fun getFileType(): FileType = CodeownersFileType.INSTANCE

    override fun toString() = "Codeowners File"
}
