package com.github.fantom.codeowners.lang

@Suppress("UnnecessaryAbstractClass")
abstract class CodeownersVisitor {
    open fun visitRule(rule: CodeownersRuleBase<*, *>) {}
}
