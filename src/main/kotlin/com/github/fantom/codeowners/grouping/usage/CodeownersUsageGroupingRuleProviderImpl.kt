package com.github.fantom.codeowners.grouping.usage

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.project.Project
import com.intellij.usages.*
import com.intellij.usages.impl.FileStructureGroupRuleProvider
import com.intellij.usages.impl.UsageGroupingRuleProviderImpl
import com.intellij.usages.impl.rules.FileGroupingRule
import com.intellij.usages.rules.UsageGroupingRule

/**
 * This class is one big hack.
 * We need to place our rule right before file level.
 * Rules are sorted by rank, the lesser the rank the close to tree root
 * But all standard rules have the same rank [Int.MAX_VALUE], so we cannot just set our rank to `FileGroupingRule#getRank()-1`
 * and, since this sorting is stable, rules with the same rank are applied in order they appear in returned array from provider
 * So what we are doing here is:
 * 1.  Replace standard grouping rule provider to control all the standard rules providing
 * 2.  Override [getAllRules] and [getActiveRules] methods to adjust ranks of standard rules
 * 2a. Wrap each standard rule except [FileGroupingRule] into [UsageGroupingRuleWrapper], that returns predefined `rank`,
 *  which allows us to utilize rules ordering based on it.
 * 2b. But there is another sort of rules, that replaces [FileGroupingRule] if grouping by file structure is enabled.
 *  And since these rules have nothing in common (no common base class or implemented interface), we cannot detect them
 *  to skip wrapping into [UsageGroupingRuleWrapper]. To workaround it, we manually disable this grouping before
 *  fetching rules from parent class and then add them manually,
 *  dropping [FileGroupingRule] (it returned because of changed settings)
 *  and partially replicating overridden method impl.
 */
// TODO handle absence of CODEOWNERS file, so no need to group by owners
class CodeownersUsageGroupingRuleProviderImpl : UsageGroupingRuleProviderImpl() {
    init {
        // remove UsageGroupingRuleProviderImpl from the list of EPs, effectively replacing it with
        // this class
        val ep = Extensions.getRootArea().getExtensionPoint(EP_NAME)
        ep.unregisterExtension(UsageGroupingRuleProviderImpl::class.java)
    }

    private class UsageGroupingRuleWrapper(private val rule: UsageGroupingRule, private val rank: Int) :
        UsageGroupingRule {
        override fun getRank() = rank

        override fun getParentGroupsFor(usage: Usage, targets: Array<out UsageTarget>): MutableList<UsageGroup> {
            return rule.getParentGroupsFor(usage, targets)
        }

        /**
         * We override this method because even if [rule] was an instance of [SingleParentUsageGroupingRule], which
         * correctly handles this method, we are not, and default impl just throws [UnsupportedOperationException]
         */
        override fun groupUsage(usage: Usage): UsageGroup? {
            return rule.groupUsage(usage)
        }
    }

    private fun prepareRulesList(project: Project, rules: Array<UsageGroupingRule>): Array<UsageGroupingRule> {
        return (
            rules
                .map {
                    if (it is FileGroupingRule) {
                        it
                    } else {
                        UsageGroupingRuleWrapper(it, it.rank - 2)
                    }
                } + CodeownersGroupingRule(project)
            ).toTypedArray()
    }

    override fun getActiveRules(
        project: Project,
        usageViewSettings: UsageViewSettings,
        presentation: UsageViewPresentation?
    ): Array<UsageGroupingRule> {
        return if (CodeownersUsageViewSettings.instance.isGroupByCodeowner) {
            if (usageViewSettings.isGroupByFileStructure) {
                usageViewSettings.isGroupByFileStructure = false
                val rules = super.getActiveRules(project, usageViewSettings, presentation)
                    .filter { it !is FileGroupingRule }
                    .map { UsageGroupingRuleWrapper(it, it.rank - 2) }
                usageViewSettings.isGroupByFileStructure = true
                val fileStructureRules =
                    FileStructureGroupRuleProvider.EP_NAME.extensionList
                        .mapNotNull { it.getUsageGroupingRule(project, usageViewSettings) }
                (rules + CodeownersGroupingRule(project) + fileStructureRules).toTypedArray()
            } else {
                prepareRulesList(project, super.getActiveRules(project, usageViewSettings, presentation))
            }
        } else {
            super.getActiveRules(project, usageViewSettings, presentation)
        }
    }

    override fun createGroupingActions(view: UsageView): Array<AnAction> {
        return super.createGroupingActions(view) + GroupByCodeownerAction()
    }

    override fun getAllRules(project: Project, usageView: UsageView?): Array<UsageGroupingRule> {
        return prepareRulesList(project, super.getAllRules(project, usageView))
    }
}
