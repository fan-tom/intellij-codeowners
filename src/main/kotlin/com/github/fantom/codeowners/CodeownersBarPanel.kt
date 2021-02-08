package com.github.fantom.codeowners

import com.github.fantom.codeowners.util.Constants
import com.intellij.ide.IdeBundle
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsContexts.StatusBarText
import com.intellij.openapi.util.NlsContexts.Tooltip
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup.WidgetState
import com.intellij.util.LineSeparator

class CodeownersBarPanel(project: Project): EditorBasedStatusBarPopup(project, false) {
    override fun ID(): String {
        return "CodeownersPanel"
    }

    private fun getWidgetStateWithIcon(state: WidgetState): WidgetState {
        state.icon = CodeownersIcons.FILE
        return state
    }

    private fun getFileOwners(file: VirtualFile): List<String>? {
        val manager = project.service<CodeownersManager>()
        return manager.getFileOwners(file)
    }

    override fun getWidgetState(file: VirtualFile?): WidgetState {
        println("getWidgetState")
        if (file == null) {
            return getWidgetStateWithIcon(WidgetState.HIDDEN)
        }
        val owners = getFileOwners(file) ?: return getWidgetStateWithIcon(WidgetState.getDumbModeState("Codeowners", "Codeowners:"))
//        val codeownersFile = getCodeownersFile(file)

//        if (codeownersFile == null) {
//            return WidgetState.HIDDEN
//        }
        //        val owners: List<String> = getFileOwners(file, codeownersFile)

        val (toolTipText, panelText) = when (owners.size) {
            0 -> Pair("No owners are set for current file", "<No owners>")
            1 -> Pair("Owner: ${owners[0]}", owners[0])
            else -> Pair("""All owners: ${owners.joinToString(", ")}""", owners[0])
        }
//        val lineSeparator = FileDocumentManager.getInstance().getLineSeparator(file, project)
//        val toolTipText = IdeBundle.message("tooltip.line.separator", StringUtil.escapeLineBreak(lineSeparator))
//        val panelText = LineSeparator.fromString(lineSeparator).toString()
        return getWidgetStateWithIcon(WidgetState(toolTipText, panelText, false))
    }

    override fun createPopup(context: DataContext?): ListPopup? {
//        val owners = getFileOwners(context?.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null)
        return null
//        TODO("Not yet implemented")
    }

    override fun createInstance(project: Project) = CodeownersBarPanel(project)

    override fun registerCustomListeners() {
        myConnection.subscribe(DumbService.DUMB_MODE, object: DumbService.DumbModeListener {
            override fun exitDumbMode() {
                println("exitDumbMode")
                update()
            }
        })
    }
}