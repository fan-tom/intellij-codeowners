package com.github.fantom.codeowners.codeInspection

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.lang.CodeownersRuleBase
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.tree.IElementType

/**
 * QuickFix action that removes specified entry handled by code inspections like [CodeownersCoverPatternInspection],
 * [CodeownersUnusedPatternInspection].
 */
class CodeownersRemoveRuleFix(rule: CodeownersRuleBase<*, *>) : LocalQuickFixAndIntentionActionOnPsiElement(rule) {

    override fun invoke(
        project: Project,
        file: PsiFile,
        editor: Editor?,
        startElement: PsiElement,
        endElement: PsiElement
    ) {
        val crlfToken = (file.fileType as CodeownersFileType).codeownersLanguage.getCrlfToken()
        if (startElement is CodeownersRuleBase<*, *>) {
            removeCrlf(startElement, crlfToken)
            startElement.delete()
        }
    }

    private fun removeCrlf(startElement: PsiElement, crlfToken: IElementType) {
        (
            TreeUtil.findSibling(
                startElement.node,
                crlfToken
            )
                ?: TreeUtil.findSiblingBackward(
                    startElement.node,
                    crlfToken
                )
            )?.psi?.delete()
    }

    override fun getText(): String = CodeownersBundle.message("quick.fix.remove.rule")

    override fun getFamilyName(): String = CodeownersBundle.message("codeInspection.group")
}
