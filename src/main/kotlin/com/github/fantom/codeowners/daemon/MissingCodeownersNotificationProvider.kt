package com.github.fantom.codeowners.daemon

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.command.CreateFileCommandAction
import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.github.fantom.codeowners.settings.CodeownersSettings
import com.github.fantom.codeowners.util.Properties
import com.github.fantom.codeowners.util.Utils
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications

// import mobi.hsz.idea.gitignore.ui.GeneratorDialog

/**
 * Editor notification provider that checks if there is .gitignore file in root directory and suggest to create one.
 */
class MissingCodeownersNotificationProvider(project: Project) :
    EditorNotifications.Provider<EditorNotificationPanel?>() {

    private val notifications = EditorNotifications.getInstance(project)
    private val settings = CodeownersSettings.getInstance()

    companion object {
        private val KEY = Key.create<EditorNotificationPanel?>("MissingGitignoreNotificationProvider")
    }

    override fun getKey() = KEY

    /**
     * Creates notification panel for given file and checks if is allowed to show the notification.
     *
     * @param file       current file
     * @param fileEditor current file editor
     * @return created notification panel
     */
    override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor, project: Project):
        EditorNotificationPanel? = when {
        !settings.missingCodeowners -> null
        Properties.isMissingCodeowners(project) -> null
        else -> {
//            val vcsDirectory = GitLanguage.INSTANCE.vcsDirectory
            val moduleRoot = Utils.getModuleRootForFile(file, project)
            val gitignoreFile = moduleRoot?.findChild(CodeownersLanguage.INSTANCE.filename)

            when {
//                vcsDirectory == null -> null
                moduleRoot == null -> null
                gitignoreFile != null -> null
//                moduleRoot.findChild(vcsDirectory)?.isDirectory ?: true -> null
                else -> createPanel(project, moduleRoot)
            }
        }
    }

    /**
     * Creates notification panel.
     *
     * @param project    current project
     * @param moduleRoot module root
     * @return notification panel
     */
    private fun createPanel(project: Project, moduleRoot: VirtualFile): EditorNotificationPanel {
        val fileType = CodeownersFileType.INSTANCE
        return EditorNotificationPanel().apply {
            text = CodeownersBundle.message("daemon.missingCodeowners")
            createActionLabel(CodeownersBundle.message("daemon.missingGitignore.create")) {
                val directory = PsiManager.getInstance(project).findDirectory(moduleRoot)
                if (directory != null) {
                    @Suppress("TooGenericException")
                    try {
                        val file = CreateFileCommandAction(project, directory, fileType).execute()
                        FileEditorManager.getInstance(project).openFile(file.virtualFile, true)
//                        GeneratorDialog(project, file).show()
                    } catch (throwable: Throwable) {
                        throwable.printStackTrace()
                    }
                }
            }
            createActionLabel(CodeownersBundle.message("daemon.cancel")) {
                Properties.setMissingCodeowners(project)
                notifications.updateAllNotifications()
            }
            try { // ignore if older SDK does not support panel icon
                icon(fileType.icon)
            } catch (ignored: NoSuchMethodError) {
            }
        }
    }
}
