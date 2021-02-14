package com.github.fantom.codeowners.languages.github

import com.intellij.lexer.FlexAdapter
import java.io.Reader

class CodeownersLexerAdapter : FlexAdapter(CodeownersLexer(null as Reader?))
