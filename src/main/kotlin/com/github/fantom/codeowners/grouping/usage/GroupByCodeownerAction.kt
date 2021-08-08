package com.github.fantom.codeowners.grouping.usage

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.CodeownersIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.usages.impl.actions.RuleAction

class GroupByCodeownerAction :
    RuleAction(CodeownersBundle.messagePointer("action.group.by.codeowner"), CodeownersIcons.FILE) {
    override fun getOptionValue(e: AnActionEvent): Boolean {
        return CodeownersUsageViewSettings.instance.isGroupByCodeowner
    }

    override fun setOptionValue(e: AnActionEvent, value: Boolean) {
        CodeownersUsageViewSettings.instance.isGroupByCodeowner = value
    }
}
