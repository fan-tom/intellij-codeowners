package com.github.fantom.codeowners.lang.kind.bitbucket

import com.github.fantom.codeowners.lang.kind.bitbucket.lexer.CodeownersLexer
import com.intellij.lexer.FlexAdapter
import java.io.Reader

class CodeownersLexerAdapter : FlexAdapter(CodeownersLexer(null as Reader?))
