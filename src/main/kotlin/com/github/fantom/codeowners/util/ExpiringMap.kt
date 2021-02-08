package com.github.fantom.codeowners.util

import com.intellij.openapi.util.Pair
import com.jetbrains.rd.util.concurrentMapOf

/**
 * Wrapper for ConcurrentHashMap that allows to expire values after given time.
 */
class ExpiringMap<K, V>(private val time: Int) {

    private val map = concurrentMapOf<K, Pair<V, Long>>()

    operator fun get(key: K): V? {
        val current = System.currentTimeMillis()
        map[key]?.let {
            if (it.getSecond() + time > current) {
                return it.getFirst()
            }
            map.remove(key)
        }
        return null
    }

    operator fun set(key: K, value: V): V {
        val current = System.currentTimeMillis()
        map[key] = Pair.create(value, current)
        return value
    }

    fun clear() {
        map.clear()
    }
}
