package com.github.fantom.codeowners.lang

@Suppress("UnnecessaryAbstractClass")
abstract class CodeownersVisitor {
    open fun visitPattern(pattern: CodeownersPatternBase<*, *>) {}
}
