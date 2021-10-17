package com.github.fantom.codeowners

import com.github.fantom.codeowners.file.type.kind.BitbucketFileType
import com.github.fantom.codeowners.file.type.kind.GithubFileType
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.util.io.ByteSequence
import com.intellij.openapi.vfs.VirtualFile

class CodeownersFileTypeDetector : FileTypeRegistry.FileTypeDetector {
    companion object {
        private val LOGGER = Logger.getInstance(CodeownersFileTypeDetector::class.java)
    }

    private fun detectBitbucketFileType(line: String): Boolean {
        return line.startsWith("@@@") || // team definition
            line.startsWith("CODEOWNERS.") || // settings
            line.startsWith("!") // negation. TODO WARN, may be imprecise
    }

    /**
     *  [com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile.isMyFileType] is already called here,
     *  so we check only file name and content
     */
    override fun detect(file: VirtualFile, firstBytes: ByteSequence, firstCharsIfText: CharSequence?): FileType? {
        LOGGER.trace("Detecting lang: ${file.path}")
        return if (file.name == CodeownersLanguage.INSTANCE.filename) {
            if (firstCharsIfText != null) {
                if (firstCharsIfText.lineSequence().any(::detectBitbucketFileType)) {
                    LOGGER.trace("Detected lang using firstCharsIfText: bb")
                    BitbucketFileType.INSTANCE
                    // firstCharsIfText may be not enough to find bb-specific pattern, so check whole file content
                } else if (file.inputStream.reader().useLines { it.any(::detectBitbucketFileType) }) {
                    LOGGER.trace("Detected lang using file content: bb")
                    BitbucketFileType.INSTANCE
                } else {
                    LOGGER.trace("Detected lang: gh")
                    GithubFileType.INSTANCE
                }
            } else {
                // CODEOWNERS file must be text, not binary
                LOGGER.trace("Detected lang: non-text")
                null
            }
        } else {
            LOGGER.trace("Detected lang: non-codeowners")
            null
        }
    }
}
