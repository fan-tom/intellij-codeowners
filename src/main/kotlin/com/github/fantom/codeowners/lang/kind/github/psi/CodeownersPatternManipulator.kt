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
class CodeownersPatternManipulator : AbstractElementManipulator<CodeownersPattern>() {

    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(pattern: CodeownersPattern, range: TextRange, newContent: String): CodeownersPattern {
        val language = pattern.language as? CodeownersLanguage ?: return pattern
        val fileType = (language.associatedFileType as CodeownersFileType)
        val file = PsiFileFactory.getInstance(pattern.project)
            .createFileFromText(language.filename, fileType, range.replace(pattern.text, newContent))

        return when (val newPattern = PsiTreeUtil.findChildOfType(file, CodeownersPattern::class.java)) {
            null -> pattern
            else -> pattern.replace(newPattern) as CodeownersPattern
        }
    }

//    override fun getRangeInElement(element: CodeownersPattern) = element.negation?.run {
//        TextRange.create(startOffsetInParent + textLength, element.textLength)
//    } ?: super.getRangeInElement(element)
}
