package com.github.fantom.codeowners.util

import com.intellij.util.containers.ContainerUtil
import java.util.concurrent.ConcurrentMap

/**
 * [ConcurrentMap] wrapper with additional ability to cache values.
 */
class CachedConcurrentMap<K, V> private constructor(private val fetcher: DataFetcher<K, V>) {

    private val map: ConcurrentMap<K, V> = ContainerUtil.createConcurrentWeakMap()

    companion object {
        fun <K, V> create(fetcher: DataFetcher<K, V>) = CachedConcurrentMap(fetcher)
    }

    operator fun get(key: K): V? {
        if (!map.containsKey(key)) {
            map[key] = fetcher.fetch(key)
        }
        return map[key]
    }

    fun remove(key: K) {
        map.remove(key)
    }

    fun clear() {
        map.clear()
    }

    /** Fetcher interface. */
    fun interface DataFetcher<K, V> {
        /**
         * Fetches data for the given key.
         *
         * @param key key
         * @return value
         */
        fun fetch(key: K): V
    }
}
