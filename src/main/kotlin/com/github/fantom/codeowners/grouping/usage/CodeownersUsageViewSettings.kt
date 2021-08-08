package com.github.fantom.codeowners.grouping.usage

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.annotations.OptionTag

/**
 * Inspired by [com.intellij.usages.UsageViewSettings]
 */
@State(name = "CodeownersUsageViewSettings", storages = [Storage("usageView.xml")])
class CodeownersUsageViewSettings(
    isGroupByCodeowner: Boolean = false
) : BaseState(), PersistentStateComponent<CodeownersUsageViewSettings> {
    companion object {
        @JvmStatic
        val instance: CodeownersUsageViewSettings
            get() = ApplicationManager.getApplication().getService(CodeownersUsageViewSettings::class.java)
    }

    @get:OptionTag("GROUP_BY_CODEOWNER")
    var isGroupByCodeowner by property(isGroupByCodeowner)

    override fun getState() = this

    override fun loadState(state: CodeownersUsageViewSettings) {
        copyFrom(state)
    }
}
