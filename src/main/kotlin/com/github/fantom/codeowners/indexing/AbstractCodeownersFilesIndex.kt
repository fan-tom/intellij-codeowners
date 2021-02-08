package com.github.fantom.codeowners.indexing

import com.intellij.openapi.project.DumbAware
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex.InputFilter
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.io.KeyDescriptor

/**
 * Abstract class of [FileBasedIndexExtension] that contains base configuration for [CodeownersFilesIndex].
 */
abstract class AbstractCodeownersFilesIndex<K, V> :
    FileBasedIndexExtension<K, V>(),
    KeyDescriptor<K>,
    DataIndexer<K, V, FileContent?>,
    InputFilter,
    DumbAware {

    override fun getIndexer() = this

    override fun getKeyDescriptor() = this

    override fun isEqual(val1: K, val2: K) = val1 == val2

    override fun getHashCode(value: K): Int = value.hashCode()

    override fun dependsOnFileContent() = true

    override fun getInputFilter() = this
}
