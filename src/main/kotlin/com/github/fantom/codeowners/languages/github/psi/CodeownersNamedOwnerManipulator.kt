package com.github.fantom.codeowners.languages.github.psi

import com.github.fantom.codeowners.CodeownersFileType
import com.github.fantom.codeowners.CodeownersLanguage
import com.github.fantom.codeowners.languages.github.psi.CodeownersNamedOwner
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException

/**
 * Entry manipulator.
 */
class CodeownersNamedOwnerManipulator : AbstractElementManipulator<CodeownersNamedOwner>() {

    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(owner: CodeownersNamedOwner, range: TextRange, newContent: String): CodeownersNamedOwner {
        val language = owner.language as? CodeownersLanguage ?: return owner
        val fileType = (language.associatedFileType as CodeownersFileType)
        val file = PsiFileFactory.getInstance(owner.project)
            .createFileFromText(language.filename, fileType, range.replace(owner.text, newContent))

        return when (val newEntry = PsiTreeUtil.findChildOfType(file, CodeownersNamedOwner::class.java)) {
            null -> owner
            else -> owner.replace(newEntry) as CodeownersNamedOwner
        }
    }
}
