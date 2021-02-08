package com.github.fantom.codeowners.util

/**
 * [MutableList] wrapper with additional ability to cache values.
 */
class CachedList<V>(private val fetcher: DataFetcher<V>): Iterable<V> {

    private var _list: List<V>? = null

    private val list: List<V> get() {
        if (_list == null) {
            _list = fetcher.fetch()
        }
        return _list!!
    }

    val size get() = list.size

    companion object {
        fun <V> create(fetcher: DataFetcher<V>) = CachedList(fetcher)
    }

    override operator fun iterator(): Iterator<V> = list.iterator()

    fun clear() {
        _list = null
    }

    /** Fetcher interface. */
    fun interface DataFetcher<V> {
        /**
         * Fetches data
         *
         * @return value
         */
        fun fetch(): List<V>?
    }
}
