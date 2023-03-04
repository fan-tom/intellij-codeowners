package com.github.fantom.codeowners.indexing

import com.github.fantom.codeowners.OwnersReference
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.builder.HashCodeBuilder
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.io.Serializable

@JvmInline
value class RegexString(val regex: String) {
    override fun toString() = regex
}

@JvmInline
value class OwnerString(val owner: String) {
    override fun toString() = owner
}

/**
 * Entry containing information about the [VirtualFile] instance of the codeowners file mapped with the collection
 * of codeowners entries with line numbers for better performance. Class is used for indexing.
 */
@Suppress("SerialVersionUIDInSerializableClass")
class CodeownersEntryOccurrence(private val url: String, val items: List<Pair<RegexString, OwnersReference>>) : Serializable {

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
                    writeUTF(it.first.regex)
                    writeInt(it.second.offset)
                    writeInt(it.second.owners.size)
                    it.second.owners.forEach { owner ->
                        writeUTF(owner.owner)
                    }
                }
            }
        }

        @Synchronized
        @Throws(IOException::class)
        fun deserialize(input: DataInput): CodeownersEntryOccurrence {
            val url = input.readUTF()
            val items = mutableListOf<Pair<RegexString, OwnersReference>>()

            if (!StringUtils.isEmpty(url)) {
                val size = input.readInt()
                repeat((0 until size).count()) {
                    val pattern = RegexString(input.readUTF())
                    val offset = input.readInt()
                    val size = input.readInt()
                    val owners = mutableListOf<OwnerString>()
                    repeat((0 until size).count()) {
                        owners.add(OwnerString(input.readUTF()))
                    }
                    items.add(Pair(pattern, OwnersReference(owners, offset)))
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
