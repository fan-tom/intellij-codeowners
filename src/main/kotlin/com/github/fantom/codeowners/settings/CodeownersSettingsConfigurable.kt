package com.github.fantom.codeowners.settings

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.ui.CodeownersSettingsPanel
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Comparing
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.VcsConfigurableProvider

/**
 * Configuration interface for [CodeownersSettings].
 */
@Suppress("UnsafeCallOnNullableType")
class CodeownersSettingsConfigurable : SearchableConfigurable, VcsConfigurableProvider {

    private val settings = CodeownersSettings.getInstance()
    private var settingsPanel = CodeownersSettingsPanel()

    override fun getDisplayName(): String = CodeownersBundle.message("settings.displayName")

    override fun getHelpTopic(): String = displayName

    override fun createComponent() = settingsPanel.panel

    override fun isModified() = settingsPanel.run {
        !Comparing.equal(settings.missingCodeowners, missingCodeowners) ||
            !Comparing.equal(settings.insertAtCursor, insertAtCursor) // ||
//            !languagesSettings.equalSettings(settings.languagesSettings)
    }

    override fun apply() {
        settingsPanel.apply {
            settings.missingCodeowners = missingCodeowners
            settings.insertAtCursor = insertAtCursor
//            settings.languagesSettings = languagesSettings.settings
        }
    }

    override fun reset() {
        settingsPanel.apply {
            missingCodeowners = settings.missingCodeowners
            insertAtCursor = settings.insertAtCursor
//            languagesSettings.update(settings.languagesSettings.clone())
        }
    }

    override fun disposeUIResources() {
        Disposer.dispose(settingsPanel)
    }

    override fun getConfigurable(project: Project) = this

    override fun getId() = helpTopic

    override fun enableSearch(option: String): Runnable? = null
}
