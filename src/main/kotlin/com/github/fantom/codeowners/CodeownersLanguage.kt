package com.github.fantom.codeowners

import com.intellij.lang.Language;

class CodeownersLanguage private constructor(): Language("CODEOWNERS") {
    companion object {
        val INSTANCE: CodeownersLanguage = CodeownersLanguage();
    }
}
