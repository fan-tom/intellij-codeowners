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

    /**
     *  [com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile.isMyFileType] is already called here,
     *  so we check only file name and content
     */
    override fun detect(file: VirtualFile, firstBytes: ByteSequence, firstCharsIfText: CharSequence?): FileType? =
        if (file.name == CodeownersLanguage.INSTANCE.filename) {
            if (firstCharsIfText != null) {
                if (
                    firstCharsIfText.contains("\n@@@") // team definition
                    ||
                    firstCharsIfText.contains("\nCODEOWNERS.") // settings
                    ||
                    firstCharsIfText.contains("\n!") // negation. TODO: WARN, may be imprecise
                ) {
                    LOGGER.trace("Detected lang: bb")
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
