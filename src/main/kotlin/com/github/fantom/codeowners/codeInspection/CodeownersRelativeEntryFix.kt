package com.github.fantom.codeowners.codeInspection

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import java.net.URI
import java.net.URISyntaxException

/**
 * QuickFix action that removes relative parts of the entry [CodeownersRelativeEntryInspection].
 */
class CodeownersRelativeEntryFix(entry: CodeownersEntry) : LocalQuickFixOnPsiElement(entry) {

    override fun invoke(project: Project, psiFile: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        if (startElement is CodeownersEntry) {
            val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
            if (document != null) {
                val start = startElement.getStartOffsetInParent()
                val text = startElement.getText()
                val fixed = getFixedPath(text)
                document.replaceString(start, start + text.length, fixed)
            }
        }
    }

    private fun getFixedPath(path: String) = path
        .run { replace("/".toRegex(), "/").replace("\\\\\\.".toRegex(), ".") }
        .run {
            try {
                URI(path).normalize().path
            } catch (e: URISyntaxException) {
                e.printStackTrace()
                this
            }
        }
        .run { replace("/\\.{1,2}/".toRegex(), "/").replace("^\\.{0,2}/".toRegex(), "") }

    override fun getText(): String = CodeownersBundle.message("quick.fix.relative.entry")

    override fun getFamilyName(): String = CodeownersBundle.message("codeInspection.group")
}
