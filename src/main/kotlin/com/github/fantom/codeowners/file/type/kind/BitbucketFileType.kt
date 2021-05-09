package com.github.fantom.codeowners.file.type.kind

import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.lang.kind.bitbucket.BitbucketLanguage

class BitbucketFileType: CodeownersFileType(BitbucketLanguage.INSTANCE) {
    companion object {
        val INSTANCE = BitbucketFileType()
    }
}