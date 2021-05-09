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
class CodeownersNamedOwnerManipulator : AbstractElementManipulator<com.github.fantom.codeowners.lang.kind.github.psi.CodeownersNamedOwner>() {

    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(owner: com.github.fantom.codeowners.lang.kind.github.psi.CodeownersNamedOwner, range: TextRange, newContent: String): com.github.fantom.codeowners.lang.kind.github.psi.CodeownersNamedOwner {
        val language = owner.language as? CodeownersLanguage ?: return owner
        val fileType = (language.associatedFileType as CodeownersFileType)
        val file = PsiFileFactory.getInstance(owner.project)
            .createFileFromText(language.filename, fileType, range.replace(owner.text, newContent))

        return when (val newEntry = PsiTreeUtil.findChildOfType(file, com.github.fantom.codeowners.lang.kind.github.psi.CodeownersNamedOwner::class.java)) {
            null -> owner
            else -> owner.replace(newEntry) as com.github.fantom.codeowners.lang.kind.github.psi.CodeownersNamedOwner
        }
    }
}
