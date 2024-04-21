package com.github.fantom.codeowners.grouping.changes

import com.github.fantom.codeowners.*
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.ui.*
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.SimpleTextAttributes
import com.intellij.vcsUtil.VcsImplUtil
import javax.swing.tree.DefaultTreeModel

// FileType objects must not outlive plugin instance when it is reloaded
typealias PersistedOwnersMap = Map<String, OwnersFileReference>

fun OwnersMap.toPersisted(): PersistedOwnersMap = this.mapKeys { it.key.name }

data class ChangesNodeData(
    val currentOwners: Map<String, OwnersList?>, // file type name to list of owners
    val prevOwners: PersistedOwnersMap?, /* set only if differs from current owners */
)

private interface ChangeNodeRenderer {
    fun append(s: String)
    fun append(s: String, linkAttributes: SimpleTextAttributes, fn: Runnable)

    class TextPresentationChangeNodeRenderer : ChangeNodeRenderer {
        private val sb = StringBuilder()

        override fun append(s: String) {
            sb.append(s)
        }

        override fun append(s: String, linkAttributes: SimpleTextAttributes, fn: Runnable) {
            sb.append(s)
        }

        val string = sb.toString()
    }

    class RichChangeNodeRenderer(private val renderer: ChangesBrowserNodeRenderer) : ChangeNodeRenderer {
        override fun append(s: String) {
            renderer.append(s)
        }

        override fun append(s: String, linkAttributes: SimpleTextAttributes, fn: Runnable) {
            renderer.append(s, linkAttributes, fn)
        }
    }
}

private class CodeownersChangesBrowserNode(
    data: ChangesNodeData,
    private val project: Project,
) : ChangesBrowserNode<ChangesNodeData>(data) {
    private fun getPrevOwnersRepr(o: PersistedOwnersMap) =
        o.values.firstNotNullOfOrNull { it.ref?.owners }?.joinToString(", ") ?: "<Unowned>"

    private fun getCurrOwnersRepr(o: Map<String, OwnersList?>) =
        o.values.firstNotNullOfOrNull { it }?.joinToString(", ") ?: "<Unowned>"

    override fun render(renderer: ChangesBrowserNodeRenderer, selected: Boolean, expanded: Boolean, hasFocus: Boolean) {
        val myRenderer = ChangeNodeRenderer.RichChangeNodeRenderer(renderer)

        render(myRenderer)

        appendCount(renderer)

        renderer.icon = CodeownersIcons.FILE
    }

    private fun render(renderer: ChangeNodeRenderer) {
        val (currentOwners, prevOwners) = getUserObject()

        val currOwners = getCurrOwnersRepr(currentOwners)
        renderer.append(currOwners)

        if (prevOwners != null) {
            renderer.append(" - moved from ")
            val repr = getPrevOwnersRepr(prevOwners)
            val (url, ref) = prevOwners.values.firstNotNullOf { it.ref?.let { _ -> Pair(it.url, it.ref)} }
            renderer.append(repr, SimpleTextAttributes.LINK_ATTRIBUTES) { goToOwner(url, ref.offset) }
        }
    }

    override fun getTextPresentation(): String {
        val renderer = ChangeNodeRenderer.TextPresentationChangeNodeRenderer()

        render(renderer)

        return renderer.string
    }

    private fun goToOwner(codeownersFileUrl: String, offset: Int) {
        val codeownersFile = VirtualFileManager.getInstance().findFileByUrl(codeownersFileUrl) ?: return
        OpenFileDescriptor(project, codeownersFile, offset).navigate(true)
    }

    override fun compareUserObjects(o2: ChangesNodeData): Int {
        // unowned last
        // TODO sort also by owner type: i.e teams first
        return o2.currentOwners.size - getUserObject().currentOwners.size
    }
}

class CodeownersChangesGroupingPolicy(val project: Project, model: DefaultTreeModel) :
    SimpleChangesGroupingPolicy<ChangesNodeData>(model) {
    private val codeownersManager = project.service<CodeownersManager>()
    private val changeListManager = ChangeListManager.getInstance(project)

    override fun createGroupRootNode(value: ChangesNodeData): ChangesBrowserNode<ChangesNodeData> {
        return CodeownersChangesBrowserNode(value, project)
    }

    override fun getGroupRootValueFor(nodePath: StaticFilePath, node: ChangesBrowserNode<*>): ChangesNodeData? {
        if (!codeownersManager.isAvailable) return null

        val prevOwnersRef = changeListManager.getChange(nodePath.filePath)?.let { change ->
            if (change.type == Change.Type.MOVED) { // includes renaming
                change
                    .beforeRevision!! // there must be a previous revision, as the file is not new
                    .file
//                    .virtualFile?. // returns null for deleted files
                    .let { codeownersManager
                        .getFileOwners(MovedVirtualFile(it, nodePath.resolve()!!))
                        .map(OwnersMap::toPersisted)
                        .getOrNull()
                    }
            } else null
        }

        val file = VcsImplUtil.findValidParentAccurately(nodePath.filePath)
        return file
            // TODO handle error properly
            ?.let { codeownersManager.getFileOwners(it).getOrNull() }
            ?.let { ownersMap ->
                val persistentOwnersMap = ownersMap.entries
                    .groupingBy { it.key.name }
                    .aggregate<_, _, OwnersList?> { _, acc, e, _ ->
                        val owners = e.value.ref?.owners
                        if (acc == null) {
                            owners
                        } else if (owners != null) {
                            acc + owners
                        } else {
                            acc
                        }
                    }

                ChangesNodeData(
                    persistentOwnersMap,
                    prevOwnersRef.takeIf { it != persistentOwnersMap }
                )
            }
    }

    internal class Factory : ChangesGroupingPolicyFactory() {
        override fun createGroupingPolicy(project: Project, model: DefaultTreeModel) =
            CodeownersChangesGroupingPolicy(project, model)
    }
}
