package com.github.fantom.codeowners.grouping

import com.github.fantom.codeowners.CodeownersIcons
import com.github.fantom.codeowners.CodeownersManager
import com.github.fantom.codeowners.OwnersSet
import com.github.fantom.codeowners.indexing.OwnerString
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyKey
import com.intellij.openapi.vcs.changes.actions.SetChangesGroupingAction
import com.intellij.openapi.vcs.changes.ui.*
import javax.swing.tree.DefaultTreeModel

class SetCodeownersChangesGroupingAction : SetChangesGroupingAction() {
    override val groupingKey: String get() = "codeowners"


    override fun update(e: AnActionEvent): Unit = super.update(e).also {
        super.update(e)
        val manager = e.project?.service<CodeownersManager>()
        e.presentation.isEnabledAndVisible = e.presentation.isEnabledAndVisible && manager?.isAvailable ?: false
    }
}

class CodeownersChangesBrowserNode(owners: OwnersSet): ChangesBrowserNode<OwnersSet>(owners) {
    override fun render(renderer: ChangesBrowserNodeRenderer, selected: Boolean, expanded: Boolean, hasFocus: Boolean) {
        super.render(renderer, selected, expanded, hasFocus)
        renderer.icon = CodeownersIcons.FILE
    }

    override fun getTextPresentation(): String {
        val owners = getUserObject()
        return if (owners.isEmpty()) {
            "<Unowned>"
        } else {
            owners.joinToString(", ")
        }
    }

    override fun compareUserObjects(o2: OwnersSet): Int {
        // unowned last
        // TODO: sort also by owner type: i.e teams first
        return o2.size - getUserObject().size
    }
}

class CodeownersChangesGroupingPolicy(val project: Project, private val model: DefaultTreeModel): BaseChangesGroupingPolicy() {
    private val codeownersManager = project.service<CodeownersManager>()

    override fun getParentNodeFor(nodePath: StaticFilePath, subtreeRoot: ChangesBrowserNode<*>): ChangesBrowserNode<*>? {
        val nextPolicyParent = nextPolicy?.getParentNodeFor(nodePath, subtreeRoot)
        if (!codeownersManager.isAvailable) return nextPolicyParent

        val file = resolveVirtualFile(nodePath)
        file?.let { codeownersManager.getFileOwners(it) }?.let { ownersRef ->
            val grandParent = nextPolicyParent ?: subtreeRoot
            val cachingRoot = getCachingRoot(grandParent, subtreeRoot)
            val owners = if (ownersRef.isEmpty()) {
                emptySet()
            } else {
                ownersRef.values.first().owners.toSet()
            }
            CODEOWNERS_CACHE.getValue(cachingRoot).getOrPut(grandParent) { mutableMapOf() }[owners]?.let { return it }

            CodeownersChangesBrowserNode(owners).let {
                it.markAsHelperNode()
                model.insertNodeInto(it, grandParent, grandParent.childCount)

                CODEOWNERS_CACHE.getValue(cachingRoot).getOrPut(grandParent) {mutableMapOf() }[owners] = it
                return it
            }
        }
        return nextPolicyParent
    }

    internal class Factory : ChangesGroupingPolicyFactory() {
        override fun createGroupingPolicy(project: Project, model: DefaultTreeModel) = CodeownersChangesGroupingPolicy(project, model)
    }

    companion object {
        val CODEOWNERS_CACHE = NotNullLazyKey.create<
                        MutableMap<
                                ChangesBrowserNode<*>,
                                MutableMap<Set<OwnerString>, ChangesBrowserNode<*>>,
                                >,
                        ChangesBrowserNode<*>
                >("ChangesTree.CodeownersCache") { mutableMapOf() }
    }
}
