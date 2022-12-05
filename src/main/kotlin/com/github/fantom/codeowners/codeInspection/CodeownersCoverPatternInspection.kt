package com.github.fantom.codeowners.codeInspection

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.lang.CodeownersFile
import com.github.fantom.codeowners.services.PatternCache
import com.github.fantom.codeowners.util.Utils
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dk.brics.automaton.BasicOperations

/**
 * Inspection tool that checks if entries are covered by others.
 */
class CodeownersCoverPatternInspection : LocalInspectionTool() {

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
        if (!Utils.isInProject(virtualFile, file.project)) {
            return null
        }
        val codeownersFile = file as? CodeownersFile ?: return null

        val problemsHolder = ProblemsHolder(manager, file, isOnTheFly)

        val cache = PatternCache.getInstance(manager.project)

        val rules = codeownersFile.getRules()
        val compiledRegexes =
            rules.map { rulePsi ->
                val glob = rulePsi.pattern.value
                cache.getOrCreateGlobRegexes2(glob)
            }

        val regexes = rules.zip(compiledRegexes)

        for ((idx, pivot) in regexes.withIndex()) {
            for ((rulePsi, current) in regexes.drop(idx + 1)) {
                ProgressManager.checkCanceled()
                // improper subsetOf
                if (BasicOperations.subsetOf(pivot.second, current)) {
                    val coveredPattern = pivot.first.pattern
                    val coveringPattern = rulePsi.pattern
                    problemsHolder.registerProblem(
                        coveredPattern,
                        message(coveringPattern, virtualFile)
                    )
                }
            }
        }

        return problemsHolder.resultsArray
    }

    override fun runForWholeFile() = true

    /**
     * Helper for inspection message generating.
     *
     * @param coveringPattern entry that covers message related
     * @param virtualFile   current working file
     * @param onTheFly      true if called during on the fly editor highlighting. Called from Inspect Code action
     * otherwise
     * @return generated message [String]
     */
    private fun message(
        coveringPattern: PsiElement,
        virtualFile: VirtualFile
    ): String {
        val path = FileUtil.toSystemIndependentName(virtualFile.path)
        return CodeownersBundle.message(
            "codeInspection.coverPattern.message",
            """<a href="#navigation/${path}:${coveringPattern.textRange.startOffset}">${coveringPattern.text}</a>"""
        )
    }
}
