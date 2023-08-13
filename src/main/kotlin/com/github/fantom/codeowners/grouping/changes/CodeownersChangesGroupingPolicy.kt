package com.github.fantom.codeowners.grouping.changes

import com.github.fantom.codeowners.CodeownersIcons
import com.github.fantom.codeowners.CodeownersManager
import com.github.fantom.codeowners.OwnersFileReference
import com.github.fantom.codeowners.OwnersMap
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyKey
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.ui.*
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.SimpleTextAttributes
import javax.swing.tree.DefaultTreeModel

// FileType objects must not outlive plugin instance when it is reloaded
typealias PersistedOwnersMap = Map<String, OwnersFileReference>

fun OwnersMap.toPersisted() = this.mapKeys { it.key.name }

private data class ChangesNodeData(
    val currentOwners: PersistedOwnersMap,
    val prevOwners: PersistedOwnersMap?, /*set only if differs from current owners */
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
    private fun getRepr(o: PersistedOwnersMap) =
        o.values.firstNotNullOfOrNull { it.ref?.owners }?.joinToString(", ") ?: "<Unowned>"

    override fun render(renderer: ChangesBrowserNodeRenderer, selected: Boolean, expanded: Boolean, hasFocus: Boolean) {
        val myRenderer = ChangeNodeRenderer.RichChangeNodeRenderer(renderer)

        render(myRenderer)

        appendCount(renderer)

        renderer.icon = CodeownersIcons.FILE
    }

    private fun render(renderer: ChangeNodeRenderer) {
        val (currentOwners, prevOwners) = getUserObject()

        val currOwners = getRepr(currentOwners)
        renderer.append(currOwners)

        if (prevOwners != null) {
            renderer.append(" - moved from ")
            val repr = getRepr(prevOwners)
            val (url, ref) = prevOwners.values.firstNotNullOf { it.ref?.let { _ -> Pair(it.url, it.ref)} }
            renderer.append(repr, SimpleTextAttributes.LINK_ATTRIBUTES, Runnable { goToOwner(url, ref.offset) })
//            "$repr -> $currOwners"
//            """<html><body>$currOwners - moved from
//                | <a href="#navigation/${prevOwnersFileRef.url}:${prevOwnersFileRef.ref!!.offset}">$repr</a>
//                | #loc</body></html>""".trimMargin()
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

class CodeownersChangesGroupingPolicy(val project: Project, private val model: DefaultTreeModel) :
    BaseChangesGroupingPolicy() {
    private val codeownersManager = project.service<CodeownersManager>()
    private val changeListManager = ChangeListManager.getInstance(project)

    @Suppress("ReturnCount")
    override fun getParentNodeFor(nodePath: StaticFilePath, subtreeRoot: ChangesBrowserNode<*>): ChangesBrowserNode<*>? {
        val nextPolicyParent = nextPolicy?.getParentNodeFor(nodePath, subtreeRoot)
        if (!codeownersManager.isAvailable) return nextPolicyParent

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

        val file = resolveVirtualFile(nodePath)
        file
            // TODO handle error properly
            ?.let { codeownersManager.getFileOwners(it).getOrNull() }
            ?.let { ownersMap ->
                val persistentOwnersMap = ownersMap.toPersisted()

                val data = ChangesNodeData(
                    persistentOwnersMap,
                    prevOwnersRef.takeIf { it != persistentOwnersMap }
                )

                val grandParent = nextPolicyParent ?: subtreeRoot
                val cachingRoot = getCachingRoot(grandParent, subtreeRoot)

                CODEOWNERS_CACHE.getValue(cachingRoot).getOrPut(grandParent) { mutableMapOf() }[data]?.let { return it }

                CodeownersChangesBrowserNode(data, project).let {
                    it.markAsHelperNode()
                    model.insertNodeInto(it, grandParent, grandParent.childCount)

                    CODEOWNERS_CACHE.getValue(cachingRoot).getOrPut(grandParent) { mutableMapOf() }[data] = it
                    return it
                }
            }
        return nextPolicyParent
    }

    internal class Factory : ChangesGroupingPolicyFactory() {
        override fun createGroupingPolicy(project: Project, model: DefaultTreeModel) =
            CodeownersChangesGroupingPolicy(project, model)
    }

    companion object {
        private val CODEOWNERS_CACHE = NotNullLazyKey.createLazyKey<
            MutableMap<
                ChangesBrowserNode<*>,
                MutableMap<ChangesNodeData, ChangesBrowserNode<*>>,
                >,
            ChangesBrowserNode<*>
            >("ChangesTree.CodeownersCache") { mutableMapOf() }
    }
}
