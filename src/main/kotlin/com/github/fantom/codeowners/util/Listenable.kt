package com.github.fantom.codeowners.util

/**
 * Abstracts a listenable object.
 */
interface Listenable<T> {

    fun addListener(listener: T)

    fun removeListener(listener: T)
}
