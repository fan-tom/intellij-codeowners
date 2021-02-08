package com.github.fantom.codeowners.codeInspection

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.language.psi.CodeownersEntry
import com.github.fantom.codeowners.language.psi.CodeownersFile
import com.github.fantom.codeowners.language.psi.CodeownersVisitor
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import com.intellij.util.containers.MultiMap

/**
 * Inspection tool that checks if entries are duplicated by others.
 */
class CodeownersDuplicateEntryInspection : LocalInspectionTool() {

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is CodeownersFile) {
            return null
        }

        val problemsHolder = ProblemsHolder(manager, file, isOnTheFly)
        val entries = MultiMap.create<String, CodeownersEntry>()
        file.acceptChildren(
            object : CodeownersVisitor() {
                override fun visitEntry(entry: CodeownersEntry) {
                    entries.putValue(entry.text, entry)
                    super.visitEntry(entry)
                }
            }
        )

        entries.entrySet().forEach { (_, value) ->
            val iterator = value.iterator()

            iterator.next()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                problemsHolder.registerProblem(
                    entry,
                    CodeownersBundle.message("codeInspection.duplicateEntry.message"),
                    CodeownersRemoveEntryFix(entry)
                )
            }
        }

        return problemsHolder.resultsArray
    }

    override fun runForWholeFile() = true
}
