package com.github.fantom.codeowners.codeInspection

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.language.psi.CodeownersEntry
import com.github.fantom.codeowners.language.psi.CodeownersPattern
import com.github.fantom.codeowners.language.psi.CodeownersTypes
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.TreeUtil

/**
 * QuickFix action that removes specified entry handled by code inspections like [CodeownersCoverEntryInspection],
 * [CodeownersDuplicateEntryInspection], [CodeownersUnusedEntryInspection].
 */
class CodeownersRemoveEntryFix(pattern: CodeownersPattern) : LocalQuickFixAndIntentionActionOnPsiElement(pattern) {

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (startElement is CodeownersPattern) {
            removeCrlf(startElement)
            startElement.delete()
        }
    }

    private fun removeCrlf(startElement: PsiElement) {
        (
            TreeUtil.findSibling(startElement.node, CodeownersTypes.CRLF) ?: TreeUtil.findSiblingBackward(
                startElement.node,
                CodeownersTypes.CRLF
            )
            )?.psi?.delete()
    }

    override fun getText(): String = CodeownersBundle.message("quick.fix.remove.entry")

    override fun getFamilyName(): String = CodeownersBundle.message("codeInspection.group")
}
