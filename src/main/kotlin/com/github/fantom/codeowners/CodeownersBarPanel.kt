package com.github.fantom.codeowners

import arrow.core.Either
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
import java.util.*

inline fun <E, T> Either<E, T>.unwrap(f: (E) -> Nothing): T =
    when (this) {
        is Either.Left -> f(this.value)
        is Either.Right -> this.value
    }

class CodeownersBarPanel(project: Project) : EditorBasedStatusBarPopup(project, false) {
    companion object {
        private val LOGGER = Logger.getInstance(CodeownersBarPanel::class.java)
    }

    private val manager = project.service<CodeownersManager>()

    override fun ID(): String {
        return "CodeownersPanel"
    }

    private fun getWidgetStateWithIcon(state: WidgetState): WidgetState {
        state.icon = CodeownersIcons.FILE
        return state
    }

//    private fun getFileOwners(file: VirtualFile): Map<CodeownersFileType, OwnersFileReference>? {
//        return manager.getFileOwners(file)
//    }
//
    @Suppress("ReturnCount", "LongMethod", "ComplexMethod")
    override fun getWidgetState(file: VirtualFile?): WidgetState {
        LOGGER.trace("getWidgetState for file $file")
        if (file == null) {
            return getWidgetStateWithIcon(WidgetState.HIDDEN)
        }
        if (!manager.isAvailable) {
            return getWidgetStateWithIcon(WidgetState.HIDDEN)
//            return getWidgetStateWithIcon(WidgetState.getDumbModeState("Codeowners", "Not available: "))
        }
        val ownersMap = manager.getFileOwners(file)
            .unwrap {
                when (it) {
                    GetFileOwnersError.InDumbMode ->
                        return getWidgetStateWithIcon(WidgetState.getDumbModeState("Codeowners", "Codeowners:"))
                    GetFileOwnersError.NotInProject ->
                        return getWidgetStateWithIcon(WidgetState("File not in project", "<Error>", true))
                    GetFileOwnersError.Disposed,
                    GetFileOwnersError.NoVirtualFile -> return getWidgetStateWithIcon(
                        WidgetState(
                            it.toString()
                                .replaceFirstChar { c ->
                                    if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString()
                                },
                            "<Error>",
                            false,
                        )
                    )
                }
            }

        val unsetTriple = Triple("Owners are unset", "<Unset>", true)
        val noOwnersTriple = Triple("No owners set for current file", "<No owners>", true)
        val ownersMapper = { owners: OwnersList, allOwners: Collection<Any> ->
            when (owners.size) {
                0 -> unsetTriple
                1 -> Triple("Owner: ${owners[0]}", owners[0].owner, true)
                else -> Triple("""All owners: ${allOwners.joinToString(", ")}""", "${owners[0].owner}...", true)
            }
        }
        val (toolTipText, panelText, actionIsAvailable) = when (ownersMap.size) {
            0 -> Triple("No CODEOWNERS files found", "<No CODEOWNERS>", false)
            1 -> {
                ownersMap.first().value.ref?.let {
                    ownersMapper(it.owners, it.owners)
                } ?: noOwnersTriple
            }
            else -> {
                val firstMatchingRule = ownersMap.firstNotNullOfOrNull { it.value.ref }
                val firstSetOwners = ownersMap
                    .mapNotNull { it.value.ref?.owners }
                    .firstOrNull { it.isNotEmpty() }
                if (firstSetOwners != null) {
                    ownersMapper(firstSetOwners, ownersMap.entries)
                } else if (firstMatchingRule != null) {
                    unsetTriple
                } else {
                    noOwnersTriple
                }
            }
        }
//        val lineSeparator = FileDocumentManager.getInstance().getLineSeparator(file, project)
//        val toolTipText = IdeBundle.message("tooltip.line.separator", StringUtil.escapeLineBreak(lineSeparator))
//        val panelText = LineSeparator.fromString(lineSeparator).toString()
        return getWidgetStateWithIcon(WidgetState(toolTipText, panelText, actionIsAvailable))
    }

    private fun goToOwner(codeownersFileUrl: String, offset: Int) {
        val codeownersFile = VirtualFileManager.getInstance().findFileByUrl(codeownersFileUrl) ?: return
        OpenFileDescriptor(project, codeownersFile, offset).navigate(true)
    }

    @Suppress("ReturnCount")
    override fun createPopup(context: DataContext): ListPopup? {
        val owners = manager
            .getFileOwners(selectedFile ?: return null)
            .unwrap { return null }
        when {
            owners.isEmpty() -> return null
            owners.size == 1 -> {
                val ref = owners.entries.first().value
                goToOwner(ref.url, ref.ref?.offset ?: 0) // go to the beginning of file if no owners set
                return null
            }
            else -> {
                data class Ref(val url: String, val offset: Int)
                return JBPopupFactory.getInstance().createListPopup(
                    object : BaseListPopupStep<Ref>("All CODEOWNERS Files", owners.values.map { Ref(it.url, it.ref?.offset ?: 0) }) {
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
        myConnection.subscribe(
            DumbService.DUMB_MODE,
            object : DumbService.DumbModeListener {
                override fun exitDumbMode() {
                    LOGGER.trace("exitDumbMode")
                    update()
                }
            }
        )
    }
}
