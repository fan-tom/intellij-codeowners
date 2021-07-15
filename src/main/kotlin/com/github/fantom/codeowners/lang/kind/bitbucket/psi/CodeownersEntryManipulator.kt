package com.github.fantom.codeowners.lang.kind.bitbucket.psi

import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException

/**
 * Entry manipulator.
 */
class CodeownersEntryManipulator : AbstractElementManipulator<com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry>() {

    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(
        entry: com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry,
        range: TextRange,
        newContent: String
    ): com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry {
        val language = entry.language as? CodeownersLanguage ?: return entry
        val fileType = (language.associatedFileType as CodeownersFileType)
        val file = PsiFileFactory.getInstance(entry.project)
            .createFileFromText(language.filename, fileType, range.replace(entry.text, newContent))

        return when (
            val newEntry = PsiTreeUtil.findChildOfType(
                file,
                com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry::class.java
            )
        ) {
            null -> entry
            else -> entry.replace(newEntry) as com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry
        }
    }

//    override fun getRangeInElement(element: CodeownersEntry) = element.negation?.run {
//        TextRange.create(startOffsetInParent + textLength, element.textLength)
//    } ?: super.getRangeInElement(element)
}
