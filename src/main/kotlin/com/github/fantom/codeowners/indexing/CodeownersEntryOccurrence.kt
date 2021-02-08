package com.github.fantom.codeowners.indexing

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.builder.HashCodeBuilder
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.io.Serializable

/**
 * Entry containing information about the [VirtualFile] instance of the codeowners file mapped with the collection
 * of codeowners entries for better performance. Class is used for indexing.
 */
class CodeownersEntryOccurrence(private val url: String, val items: List<Pair<String, List<String>>>) : Serializable {

    /**
     * Returns current [VirtualFile].
     *
     * @return current file
     */
    var file: VirtualFile? = null
        get() {
            if (field == null && url.isNotEmpty()) {
                field = VirtualFileManager.getInstance().findFileByUrl(url)
            }
            return field
        }
        private set

    companion object {
        @Synchronized
        @Throws(IOException::class)
        fun serialize(out: DataOutput, entry: CodeownersEntryOccurrence) {
            out.run {
                writeUTF(entry.url)
                writeInt(entry.items.size)
                entry.items.forEach {
                    writeUTF(it.first)
                    writeInt(it.second.size)
                    it.second.forEach { owner ->
                        writeUTF(owner)
                    }
                }
            }
        }

        @Synchronized
        @Throws(IOException::class)
        fun deserialize(input: DataInput): CodeownersEntryOccurrence {
            val url = input.readUTF()
            val items = mutableListOf<Pair<String, List<String>>>()

            if (!StringUtils.isEmpty(url)) {
                val size = input.readInt()
                repeat((0 until size).count()) {
                    val pattern = input.readUTF()
                    val size = input.readInt()
                    val owners = mutableListOf<String>()
                    repeat((0 until size).count()) {
                        owners.add(input.readUTF())
                    }
                    items.add(Pair(pattern, owners))
                }
            }
            return CodeownersEntryOccurrence(url, items)
        }
    }

    override fun hashCode() = HashCodeBuilder().append(url).apply {
        items.forEach { append(it.first).append(it.second) }
    }.toHashCode()

    override fun equals(other: Any?) = when {
        other !is CodeownersEntryOccurrence -> false
        url != other.url || items.size != other.items.size -> false
        else -> items.indices.find { items[it].toString() != other.items[it].toString() } == null
    }
}
