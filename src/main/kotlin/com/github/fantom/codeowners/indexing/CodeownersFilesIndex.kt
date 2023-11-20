package com.github.fantom.codeowners.indexing

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.lang.CodeownersFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*
import com.intellij.util.indexing.FileBasedIndex.InputFilter
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.KeyDescriptor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.util.*

/**
 * Implementation of [AbstractCodeownersFilesIndex] that allows to index all codeowners files content using native
 * IDE mechanisms and increase indexing performance.
 */
@Suppress("TooManyFunctions")
class CodeownersFilesIndex :
    FileBasedIndexExtension<CodeownersFileType, CodeownersEntryOccurrence>(),
    KeyDescriptor<CodeownersFileType>,
    DataIndexer<CodeownersFileType, CodeownersEntryOccurrence, FileContent>,
    InputFilter,
    DumbAware {
    override fun getIndexer() = this

    override fun getKeyDescriptor() = this

    override fun isEqual(val1: CodeownersFileType, val2: CodeownersFileType) = val1 == val2

    override fun getHashCode(value: CodeownersFileType): Int = value.hashCode()

    override fun dependsOnFileContent() = true

    override fun getInputFilter() = this

    companion object {
        private val LOGGER = Logger.getInstance(CodeownersFilesIndex::class.java)
        val KEY = ID.create<CodeownersFileType, CodeownersEntryOccurrence>("CodeownersFilesIndex")
        private const val VERSION = 2
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
        fun getEntries(project: Project, fileType: CodeownersFileType): List<CodeownersEntryOccurrence> {
            LOGGER.trace(">getEntries for file type $fileType in project ${project.name}")
//            try {
            if (ApplicationManager.getApplication().isReadAccessAllowed) {
                val scope = CodeownersSearchScope[project]
                val res = FileBasedIndex.getInstance().getValues(KEY, fileType, scope)
                LOGGER.trace("<getEntries ${project.name} ${res.size}")
                return res
            }
//            } catch (ignored: RuntimeException) {
//            }
            LOGGER.trace("<getEntries ${project.name} null")
            return emptyList()
        }
    }

    override fun getName(): ID<CodeownersFileType, CodeownersEntryOccurrence> = KEY

    @Suppress("ReturnCount")
    override fun map(inputData: FileContent): Map<CodeownersFileType, CodeownersEntryOccurrence> {
        val inputDataPsi = try {
            inputData.psiFile as? CodeownersFile
        } catch (e: Exception) {
            // if there is some stale indices
            // inputData.getPsiFile() could throw exception that should be avoided
            return emptyMap()
        } ?: return emptyMap()

//        val items = mutableListOf<Pair<RegexString, List<OwnerString>>>()
//        inputDataPsi.acceptChildren(
//            object : com.github.fantom.codeowners.lang.kind.github.psi.CodeownersVisitor() {
//                override fun visitPattern(entry: com.github.fantom.codeowners.lang.kind.github.psi.CodeownersPattern) {
//                    val regex = entry.entryFile.regex(false)
//                    items.add(Pair(RegexString(regex), entry.owners.ownerList.map{ OwnerString(it.text) }))
//                }
//            }
//        )

        val codeownersEntryOccurrence = CodeownersEntryOccurrence(inputData.file.url, inputDataPsi.getRulesList())

        return mapOf(
            CodeownersFileType.INSTANCE to codeownersEntryOccurrence,
            (inputData.fileType as CodeownersFileType) to codeownersEntryOccurrence,
        )
    }

    @Synchronized
    @Throws(IOException::class)
    override fun save(out: DataOutput, value: CodeownersFileType) {
        out.writeUTF(value.language.id)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun read(input: DataInput): CodeownersFileType = // CodeownersFileType.INSTANCE
        input.readUTF().run {
            CodeownersBundle.LANGUAGES
                .asSequence()
                .map { it.fileType }
                .firstOrNull { it.languageName == this }
                .let { it ?: CodeownersFileType.INSTANCE }
        }

    override fun getValueExternalizer() = DATA_EXTERNALIZER

    override fun getVersion() = VERSION

    override fun acceptInput(file: VirtualFile) =
        file.fileType is CodeownersFileType // || CodeownersManager.FILE_TYPES_ASSOCIATION_QUEUE.containsKey(file.name)
}
