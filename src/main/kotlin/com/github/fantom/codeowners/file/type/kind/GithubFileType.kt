package com.github.fantom.codeowners.file.type.kind

import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.lang.kind.github.GithubLanguage

class GithubFileType: CodeownersFileType(GithubLanguage.INSTANCE) {
    companion object {
        val INSTANCE = GithubFileType()
    }
}