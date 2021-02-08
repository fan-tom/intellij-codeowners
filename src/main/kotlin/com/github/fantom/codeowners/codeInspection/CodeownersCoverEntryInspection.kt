package com.github.fantom.codeowners.codeInspection

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.language.psi.CodeownersEntry
import com.github.fantom.codeowners.language.psi.CodeownersFile
import com.github.fantom.codeowners.services.CodeownersMatcher
import com.github.fantom.codeowners.util.Constants
import com.github.fantom.codeowners.util.Glob
import com.github.fantom.codeowners.util.Utils
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Pair
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

/**
 * Inspection tool that checks if entries are covered by others.
 */
class CodeownersCoverEntryInspection : LocalInspectionTool() {

    /**
     * Reports problems at file level. Checks if entries are covered by other entries.
     *
     * @param file       current working file to check
     * @param manager    [InspectionManager] to ask for [ProblemDescriptor]'s from
     * @param isOnTheFly true if called during on the fly editor highlighting. Called from Inspect Code action otherwise
     * @return `null` if no problems found or not applicable at file level
     */
    @Suppress("ComplexMethod", "NestedBlockDepth", "ReturnCount")
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val virtualFile = file.virtualFile
        if (file !is CodeownersFile || !Utils.isInProject(virtualFile, file.getProject())) {
            return null
        }
        val contextDirectory = virtualFile.parent ?: return null
        val problemsHolder = ProblemsHolder(manager, file, isOnTheFly)

        val ignored = mutableSetOf<String>()
        val unignored = mutableSetOf<String>()
        val result = mutableListOf<Pair<CodeownersEntry, CodeownersEntry>>()
        val map = mutableMapOf<CodeownersEntry, Set<String>>()

        val entries = file.findChildrenByClass(CodeownersEntry::class.java)
        val matcher = file.project.service<CodeownersMatcher>()
        val matchedMap = getPathsSet(contextDirectory, entries, matcher)

        entries.forEach entries@{ entry ->
            ProgressManager.checkCanceled()
            val matched = matchedMap[entry] ?: return@entries
            val intersection: Collection<String>

            ignored.addAll(matched)
            intersection = unignored.intersect(matched)

            if (unignored.removeAll(intersection)) {
                return@entries
            }

            map.keys.forEach recent@{ recent ->
                ProgressManager.checkCanceled()
                val recentValues = map[recent] ?: return@recent
                if (recentValues.isEmpty() || matched.isEmpty()) {
                    return@recent
                }
//                if (entry.isNegated == recent.isNegated) {
                    if (recentValues.containsAll(matched)) {
                        result.add(Pair.create(recent, entry))
                    } else if (matched.containsAll(recentValues)) {
                        result.add(Pair.create(entry, recent))
                    }
//                } else if (intersection.containsAll(recentValues)) {
//                    result.add(Pair.create(entry, recent))
//                }
            }
            map[entry] = matched
        }

        result.forEach { pair ->
            problemsHolder.registerProblem(
                pair.second,
                message(pair.first, virtualFile, isOnTheFly),
                CodeownersRemoveEntryFix(pair.second)
            )
        }

        return problemsHolder.resultsArray
    }

    /**
     * Returns the paths list for the given [CodeownersEntry] array in [VirtualFile] context.
     * Stores fetched data in [.cacheMap] to limit the queries to the files tree.
     *
     * @param contextDirectory current context
     * @param entries          to check
     * @return paths list
     */
    private fun getPathsSet(contextDirectory: VirtualFile, entries: Array<CodeownersEntry>, matcher: CodeownersMatcher) =
        mutableMapOf<CodeownersEntry, Set<String>>().apply {
            val found = Glob.findAsPaths(contextDirectory, entries.toList(), matcher, true)
            found.forEach { (key, value) ->
                ProgressManager.checkCanceled()
                this[key] = value
            }
        }

    override fun runForWholeFile() = true

    /**
     * Helper for inspection message generating.
     *
     * @param coveringEntry entry that covers message related
     * @param virtualFile   current working file
     * @param onTheFly      true if called during on the fly editor highlighting. Called from Inspect Code action
     * otherwise
     * @return generated message [String]
     */
    private fun message(coveringEntry: CodeownersEntry, virtualFile: VirtualFile, onTheFly: Boolean): String {
        val document = FileDocumentManager.getInstance().getDocument(virtualFile)

        return if (onTheFly || document == null) {
            CodeownersBundle.message("codeInspection.coverEntry.message", "\'" + coveringEntry.text + "\'")
        } else {
            CodeownersBundle.message(
                "codeInspection.coverEntry.message",
                "<a href=\"" + virtualFile.url + Constants.HASH + coveringEntry.textRange.startOffset + "\">" + coveringEntry.text + "</a>"
            )
        }
    }
}
