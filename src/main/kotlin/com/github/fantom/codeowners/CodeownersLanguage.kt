package com.github.fantom.codeowners

import com.intellij.lang.Language;

class CodeownersLanguage private constructor(): Language("CODEOWNERS") {
    val filename = "CODEOWNERS"

    companion object {
        val INSTANCE: CodeownersLanguage = CodeownersLanguage();
    }
}
