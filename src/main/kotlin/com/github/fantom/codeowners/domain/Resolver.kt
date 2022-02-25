package com.github.fantom.codeowners.domain

interface Assignment {
    fun matches(path: String): Boolean
}

class Resolver {
    fun <T : Assignment> resolve(list: List<T>, path: String): T? {
        return list.lastOrNull { it.matches(path) }
    }
}
