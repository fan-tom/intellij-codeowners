package com.github.fantom.codeowners

import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup
import com.jetbrains.rd.util.first

class CodeownersBarPanel(project: Project) : EditorBasedStatusBarPopup(project, false) {
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

    private fun getFileOwners(file: VirtualFile): Map<CodeownersFileType, OwnersFileReference>? {
        val manager = project.service<CodeownersManager>()
        return manager.getFileOwners(file)
    }

    override fun getWidgetState(file: VirtualFile?): WidgetState {
        LOGGER.trace("getWidgetState")
        if (file == null) {
            return getWidgetStateWithIcon(WidgetState.HIDDEN)
        }
        val ownersMap = getFileOwners(file)
                ?: return getWidgetStateWithIcon(WidgetState.getDumbModeState("Codeowners", "Codeowners:"))

        val (toolTipText, panelText) = when (ownersMap.size) {
            0 -> Pair("No CODEOWNERS files found", "<No CODEOWNERS>")
            1 -> {
                val owners = ownersMap.first().value.ref.owners
                when (owners.size) {
                    0 -> Pair("No owners are set for current file", "<No owners>")
                    1 -> Pair("Owner: ${owners[0]}", owners[0].owner)
                    else -> Pair("""All owners: ${owners.joinToString(", ")}""", "${owners[0].owner}...")
                }
            }
            else -> Pair("""All owners: ${ownersMap.entries.joinToString(", ")}""", ownersMap.first().value.ref.owners[0].owner)
        }
//        val lineSeparator = FileDocumentManager.getInstance().getLineSeparator(file, project)
//        val toolTipText = IdeBundle.message("tooltip.line.separator", StringUtil.escapeLineBreak(lineSeparator))
//        val panelText = LineSeparator.fromString(lineSeparator).toString()
        return getWidgetStateWithIcon(WidgetState(toolTipText, panelText, true))
    }

    private fun goToOwner(codeownersFileUrl: String, offset: Int) {
        val codeownersFile = VirtualFileManager.getInstance().findFileByUrl(codeownersFileUrl) ?: return
        OpenFileDescriptor(project, codeownersFile, offset).navigate(true)
    }

    override fun createPopup(context: DataContext?): ListPopup? {
        val owners = getFileOwners(selectedFile ?: return null)
        when {
            owners === null || owners.isEmpty() -> return null
            owners.size == 1 -> {
                val ref = owners.entries.first().value
                goToOwner(ref.url ?: return null, ref.ref.offset)
                return null
            }
            else -> {
                data class Ref(val url: String, val offset: Int)
                return JBPopupFactory.getInstance().createListPopup(
                        object : BaseListPopupStep<Ref>("All CODEOWNERS Files", owners.values.map { Ref(it.url!!, it.ref.offset) }) {
                            override fun onChosen(selectedValue: Ref?, finalChoice: Boolean): PopupStep<*>? {
                                selectedValue?.also {
                                    goToOwner(it.url, it.offset)
                                }
                                return super.onChosen(selectedValue, finalChoice)
                            }
                        }
                )
            }
        }
    }

    override fun createInstance(project: Project) = CodeownersBarPanel(project)

    override fun registerCustomListeners() {
        myConnection.subscribe(DumbService.DUMB_MODE, object : DumbService.DumbModeListener {
            override fun exitDumbMode() {
                LOGGER.trace("exitDumbMode")
                update()
            }
        })
    }
}
