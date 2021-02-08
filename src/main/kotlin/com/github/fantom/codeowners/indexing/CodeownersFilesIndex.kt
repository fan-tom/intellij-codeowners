package com.github.fantom.codeowners.indexing

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.CodeownersFileType
import com.github.fantom.codeowners.CodeownersManager
import com.github.fantom.codeowners.language.psi.CodeownersFile
import com.github.fantom.codeowners.language.psi.CodeownersPattern
import com.github.fantom.codeowners.language.psi.CodeownersVisitor
import com.github.fantom.codeowners.util.Glob
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*
import com.intellij.util.indexing.FileBasedIndex.InputFilter
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.DifferentSerializableBytesImplyNonEqualityPolicy
import com.intellij.util.io.KeyDescriptor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.util.Collections

/**
 * Implementation of [AbstractCodeownersFilesIndex] that allows to index all codeowners files content using native
 * IDE mechanisms and increase indexing performance.
 */
class CodeownersFilesIndex :
        FileBasedIndexExtension<CodeownersFileType, CodeownersEntryOccurrence>(),
        KeyDescriptor<CodeownersFileType>,
        DataIndexer<CodeownersFileType, CodeownersEntryOccurrence, FileContent?>,
        InputFilter,
        DumbAware
{
    override fun getIndexer() = this

    override fun getKeyDescriptor() = this

    override fun isEqual(val1: CodeownersFileType, val2: CodeownersFileType) = val1 == val2

    override fun getHashCode(value: CodeownersFileType): Int = value.hashCode()

    override fun dependsOnFileContent() = true

    override fun getInputFilter() = this

    companion object {
        val KEY = ID.create<CodeownersFileType, CodeownersEntryOccurrence>("CodeownersFilesIndex")
        private const val VERSION = 1
        private val DATA_EXTERNALIZER = object : DataExternalizer<CodeownersEntryOccurrence> {

            @Throws(IOException::class)
            override fun save(out: DataOutput, entry: CodeownersEntryOccurrence) = CodeownersEntryOccurrence.serialize(out, entry)

            @Throws(IOException::class)
            override fun read(input: DataInput) = CodeownersEntryOccurrence.deserialize(input)
        }

        /**
         * Returns collection of indexed [CodeownersEntryOccurrence] for given [Project] and [CodeownersFileType].
         *
         * @param project  current project
         * @param fileType filetype
         * @return [CodeownersEntryOccurrence] collection
         */
        fun getEntries(project: Project): List<CodeownersEntryOccurrence>? {
            println(">getEntries ${project.name}")
//            try {
                if (ApplicationManager.getApplication().isReadAccessAllowed) {
                    val scope = CodeownersSearchScope[project]
                    val res = FileBasedIndex.getInstance().getValues(KEY, CodeownersFileType.INSTANCE, scope)
                    println(">getEntries ${project.name} ${res.size}")
                    return res
                }
//            } catch (ignored: RuntimeException) {
//            }
            println(">getEntries ${project.name} null")
            return null//emptyList()
        }
    }

    override fun getName(): ID<CodeownersFileType, CodeownersEntryOccurrence> = KEY

    @Suppress("ReturnCount")
    override fun map(inputData: FileContent): Map<CodeownersFileType, CodeownersEntryOccurrence> {
        val inputDataPsi = try {
            inputData.psiFile
        } catch (e: Exception) {
            // if there is some stale indices
            // inputData.getPsiFile() could throw exception that should be avoided
            return emptyMap()
        }
        if (inputDataPsi !is CodeownersFile) {
            return emptyMap()
        }

        val items = mutableListOf<Pair<String, List<String>>>()
        inputDataPsi.acceptChildren(
            object : CodeownersVisitor() {
                override fun visitPattern(entry: CodeownersPattern) {
                    val regex = Glob.createRegex(entry.entryFile.value, false)
                    items.add(Pair(regex, entry.owners.ownerList.map{ it.text }))
                }
            }
        )

        return Collections.singletonMap(
            (inputData.fileType as CodeownersFileType),
            CodeownersEntryOccurrence(inputData.file.url, items)
        )
    }

    @Synchronized
    @Throws(IOException::class)
    override fun save(out: DataOutput, value: CodeownersFileType) {
        out.writeUTF(value.language.id)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun read(input: DataInput): CodeownersFileType = CodeownersFileType.INSTANCE
//    = input.readUTF().run {
//        CodeownersBundle.LANGUAGES
//            .asSequence()
//            .map { it.fileType }
//            .firstOrNull { it.languageName == this }
//            .let { it ?: CodeownersFileType.INSTANCE }
//    }

    override fun getValueExternalizer() = DATA_EXTERNALIZER

    override fun getVersion() = VERSION

    override fun acceptInput(file: VirtualFile) =
        file.fileType is CodeownersFileType //|| CodeownersManager.FILE_TYPES_ASSOCIATION_QUEUE.containsKey(file.name)
}
