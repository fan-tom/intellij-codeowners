package com.github.fantom.codeowners.daemon

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.lang.CodeownersEntryBase
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.util.PlatformIcons
import com.github.fantom.codeowners.services.CodeownersMatcher
import com.github.fantom.codeowners.util.Glob
import com.github.fantom.codeowners.util.Utils

/**
 * [LineMarkerProvider] that marks entry lines with directory icon if they point to the directory in virtual system.
 */
class CodeownersDirectoryMarkerProvider : LineMarkerProvider {

    private val cache = mutableMapOf<String, Boolean>()

    /**
     * Returns [LineMarkerInfo] with set [PlatformIcons.FOLDER_ICON] if entry points to the directory.
     *
     * @param element current element
     * @return `null` if entry is not a directory
     */
    @Suppress("ReturnCount")
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is CodeownersEntryBase) {
            return null
        }
        var isDirectory = element.isDirectory

        if (!isDirectory) {
            val key = element.getText()
            if (cache.containsKey(key)) {
                isDirectory = cache[key] ?: false
            } else {
                val parent = element.getContainingFile().virtualFile.parent ?: return null
                val project = element.getProject()
                Utils.getModuleForFile(parent, project) ?: return null

                val matcher = project.service<CodeownersMatcher>()
                val file = Glob.findOne(parent, element, matcher)
                cache[key] = file != null && file.isDirectory.also { isDirectory = it }
            }
        }

        return if (isDirectory) LineMarkerInfo(
                element.getFirstChild(),
                element.getTextRange(),
                PlatformIcons.FOLDER_ICON,
                null,
                null,
                GutterIconRenderer.Alignment.CENTER,
                CodeownersBundle.messagePointer("daemon.lineMarker.directory")
        )
        else null
    }
}
