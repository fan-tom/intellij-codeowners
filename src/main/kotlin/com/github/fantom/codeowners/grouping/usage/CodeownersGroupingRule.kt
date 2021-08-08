package com.github.fantom.codeowners.grouping.usage

import com.github.fantom.codeowners.CodeownersIcons
import com.github.fantom.codeowners.CodeownersManager
import com.github.fantom.codeowners.OwnersSet
import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.openapi.components.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.usages.*
import com.intellij.usages.impl.rules.UsageGroupBase
import com.intellij.usages.rules.*
import javax.swing.Icon

class CodeownersGroupingRule(project: Project) :
    SingleParentUsageGroupingRule(),
    DumbAware,
    UsageGroupingRuleEx {
    private val codeownersManager = project.service<CodeownersManager>()
    override fun getParentGroupFor(usage: Usage, targets: Array<out UsageTarget>): UsageGroup? {
        return (usage as? UsageInFile)?.file
            ?.let { (it as? VirtualFileWindow)?.delegate ?: it }
            ?.let(::getGroupForFile)
    }

    private fun getGroupForFile(virtualFile: VirtualFile): UsageGroup? {
        return codeownersManager.getFileOwners(virtualFile)?.values?.first()?.ref?.owners?.toSet()
            ?.let(::CodeownersGroup)
    }

    override fun getGroupingActionId(): String {
        return "UsageGrouping.Codeowner"
    }

    private data class CodeownersGroup(private val owners: OwnersSet) : UsageGroupBase(1) {
        override fun getIcon(): Icon {
            return CodeownersIcons.FILE
        }

        override fun getPresentableGroupText(): String {
            return if (owners.isEmpty()) {
                "<Unowned>"
            } else {
                owners.joinToString(", ")
            }
        }
    }

    override fun getRank(): Int {
        return super<UsageGroupingRuleEx>.getRank() - 1
    }
}
