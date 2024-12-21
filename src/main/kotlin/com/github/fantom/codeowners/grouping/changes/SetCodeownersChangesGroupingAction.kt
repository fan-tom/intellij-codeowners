package com.github.fantom.codeowners.grouping.changes

import com.github.fantom.codeowners.CodeownersManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.vcs.changes.actions.SetChangesGroupingAction

class SetCodeownersChangesGroupingAction : SetChangesGroupingAction() {
    override val groupingKey: String get() = "codeowners"

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        super.update(e)
        val manager = e.project?.service<CodeownersManager>()
        e.presentation.isEnabledAndVisible = e.presentation.isEnabledAndVisible && manager?.isAvailable ?: false
    }
}
