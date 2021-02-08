package com.github.fantom.codeowners.util

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NonNls

/**
 * [Properties] util class that holds project specified settings using [PropertiesComponent].
 */
object Properties {

    /** Ignore missing gitignore property key.  */
    @NonNls
    private val MISSING_CODEOWNERS = "missing_codeowners"

    /**
     * Checks value of [.MISSING_CODEOWNERS] key in [PropertiesComponent].
     *
     * @param project current project
     * @return [.MISSING_CODEOWNERS] value
     */
    fun isMissingCodeowners(project: Project) = project.service<PropertiesComponent>()
        .getBoolean(MISSING_CODEOWNERS, false)

    /**
     * Sets value of [.MISSING_CODEOWNERS] key in [PropertiesComponent] to `true`.
     *
     * @param project current project
     */
    fun setMissingCodeowners(project: Project) = project.service<PropertiesComponent>()
        .setValue(MISSING_CODEOWNERS, true)
}
