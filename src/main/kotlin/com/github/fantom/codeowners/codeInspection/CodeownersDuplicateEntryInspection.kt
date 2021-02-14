package com.github.fantom.codeowners.codeInspection

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.languages.github.psi.CodeownersFile
import com.github.fantom.codeowners.languages.github.psi.CodeownersPattern
import com.github.fantom.codeowners.languages.github.psi.CodeownersVisitor
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiFile
import com.intellij.util.containers.MultiMap

/**
 * Inspection tool that checks if entries are duplicated by others.
 */
class CodeownersDuplicateEntryInspection : LocalInspectionTool() {
    private val LOG = Logger.getInstance(LocalInspectionTool::class.java)

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is CodeownersFile) {
            return null
        }
        LOG.warn("Checking file for duplicates ${file.name}")

        val problemsHolder = ProblemsHolder(manager, file, isOnTheFly)
        val entries = MultiMap.create<String, CodeownersPattern>()
        file.acceptChildren(
            object : CodeownersVisitor() {
                override fun visitPattern(pattern: CodeownersPattern) {
                    val entry = pattern.entryFile
                    LOG.warn("Remembering entry ${entry.text}")
                    entries.putValue(entry.text, pattern)
                    super.visitEntry(entry)
                }
            }
        )

        LOG.warn("Remembered entries $entries")
        entries.entrySet().forEach { (_, value) ->
            val iterator = value.iterator()

            iterator.next()
            while (iterator.hasNext()) {
                val pattern = iterator.next()
                problemsHolder.registerProblem(
                    pattern,
                    CodeownersBundle.message("codeInspection.duplicateEntry.message"),
                    CodeownersRemoveEntryFix(pattern)
                )
            }
        }

        return problemsHolder.resultsArray
    }

    override fun runForWholeFile() = true
}
