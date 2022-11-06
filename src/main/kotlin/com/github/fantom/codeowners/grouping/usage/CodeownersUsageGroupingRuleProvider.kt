package com.github.fantom.codeowners.grouping.usage

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.usages.UsageView
import com.intellij.usages.UsageViewPresentation
import com.intellij.usages.UsageViewSettings
import com.intellij.usages.rules.UsageGroupingRule
import com.intellij.usages.rules.UsageGroupingRuleProviderEx

// TODO handle absence of CODEOWNERS file, so no need to group by owners
class CodeownersUsageGroupingRuleProvider : UsageGroupingRuleProviderEx {
    override fun getActiveRules(
        project: Project,
        usageViewSettings: UsageViewSettings,
        presentation: UsageViewPresentation?
    ): Array<UsageGroupingRule> {
        return if (CodeownersUsageViewSettings.instance.isGroupByCodeowner) {
            arrayOf(CodeownersGroupingRule(project))
        } else {
            UsageGroupingRule.EMPTY_ARRAY
        }
    }

    override fun createGroupingActions(view: UsageView): Array<AnAction> {
        return arrayOf(GroupByCodeownerAction())
    }

    override fun getAllRules(project: Project, usageView: UsageView?): Array<UsageGroupingRule> {
        return arrayOf(CodeownersGroupingRule(project))
    }
}
