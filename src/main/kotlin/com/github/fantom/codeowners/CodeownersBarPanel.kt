package com.github.fantom.codeowners

import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.indexing.OwnerString
import com.intellij.ide.util.EditorGotoLineNumberDialog
import com.intellij.ide.util.GotoLineNumberDialog
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.ListPopupStep
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.ui.UIBundle
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.Consumer
import com.jetbrains.rd.util.first
import java.awt.Component
import java.awt.Dimension
import java.awt.Point
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.event.ListSelectionListener

class CodeownersBarPanel(project: Project): EditorBasedStatusBarPopup(project, false) {
    companion object {
        private val LOGGER = Logger.getInstance(CodeownersBarPanel::class.java)
    }

    override fun ID(): String {
        return "CodeownersPanel"
    }

    private fun getWidgetStateWithIcon(state: WidgetState): WidgetState {
        state.icon = CodeownersIcons.FILE
        return state
    }

    private fun getFileOwners(file: VirtualFile): Map<CodeownersFileType, OwnersReference>? {
        val manager = project.service<CodeownersManager>()
        return manager.getFileOwners(file)
    }

    override fun getWidgetState(file: VirtualFile?): WidgetState {
        LOGGER.trace("getWidgetState")
        if (file == null) {
            return getWidgetStateWithIcon(WidgetState.HIDDEN)
        }
        val ownersMap = getFileOwners(file) ?: return getWidgetStateWithIcon(WidgetState.getDumbModeState("Codeowners", "Codeowners:"))
//        val codeownersFile = getCodeownersFile(file)

//        if (codeownersFile == null) {
//            return WidgetState.HIDDEN
//        }
        //        val owners: List<String> = getFileOwners(file, codeownersFile)

        val (toolTipText, panelText) = when (ownersMap.size) {
            0 -> Pair("No CODEOWNERS files found", "<No CODEOWNERS>")
            1 -> {
                val owners = ownersMap.first().value.owners
                when (owners.size) {
                    0 -> Pair("No owners are set for current file", "<No owners>")
                    1 -> Pair("Owner: ${owners[0]}", owners[0].owner)
                    else -> Pair("""All owners: ${owners.joinToString(", ")}""", "${owners[0].owner}...")
                }
            }
            else -> Pair("""All owners: ${ownersMap.entries.joinToString(", ")}""", ownersMap.first().value.owners[0].owner)
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
                LOGGER.trace("exitDumbMode")
                update()
            }
        })
    }
}