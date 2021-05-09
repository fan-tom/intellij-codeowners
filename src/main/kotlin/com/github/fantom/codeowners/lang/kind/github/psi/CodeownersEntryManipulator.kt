package com.github.fantom.codeowners.lang.kind.github.psi

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
class CodeownersEntryManipulator : AbstractElementManipulator<CodeownersEntry>() {

    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(entry: CodeownersEntry, range: TextRange, newContent: String): CodeownersEntry {
        val language = entry.language as? CodeownersLanguage ?: return entry
        val fileType = (language.associatedFileType as CodeownersFileType)
        val file = PsiFileFactory.getInstance(entry.project)
            .createFileFromText(language.filename, fileType, range.replace(entry.text, newContent))

        return when (val newEntry = PsiTreeUtil.findChildOfType(file, CodeownersEntry::class.java)) {
            null -> entry
            else -> entry.replace(newEntry) as CodeownersEntry
        }
    }

//    override fun getRangeInElement(element: CodeownersEntry) = element.negation?.run {
//        TextRange.create(startOffsetInParent + textLength, element.textLength)
//    } ?: super.getRangeInElement(element)
}
