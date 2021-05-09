package com.github.fantom.codeowners.lang.kind.github

import com.github.fantom.codeowners.lang.kind.github.lexer.CodeownersLexer
import com.intellij.lexer.FlexAdapter
import java.io.Reader

class CodeownersLexerAdapter : FlexAdapter(CodeownersLexer(null as Reader?))
