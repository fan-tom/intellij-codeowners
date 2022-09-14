package com.github.fantom.codeowners.codeInspection

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.lang.CodeownersPatternBase
import com.github.fantom.codeowners.lang.CodeownersVisitor
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceOwner

/**
 * Inspection tool that checks if entries are unused - does not cover any file or directory.
 */
class CodeownersUnusedEntryInspection : LocalInspectionTool() {

    /**
     * Checks if entries are related to any file.
     *
     * @param holder     where visitor will register problems found.
     * @param isOnTheFly true if inspection was run in non-batch mode
     * @return not-null visitor for this inspection
     */
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val visitor = object : CodeownersVisitor() {
            override fun visitPattern(pattern: CodeownersPatternBase<*, *>) {
                val entry = pattern.entry
                val lastReference = entry.references.lastOrNull {
                    it is FileReferenceOwner
                } ?: return
                val hasNoTargets = (lastReference as PsiPolyVariantReference).multiResolve(false).isEmpty()
                if (hasNoTargets) {
                    holder.registerProblem(
                        entry,
                        CodeownersBundle.message("codeInspection.unusedEntry.message"),
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                        CodeownersRemoveEntryFix(pattern)
                    )
                }
            }
        }

        return (holder.file.fileType as CodeownersFileType).codeownersLanguage.getVisitor(visitor)!!
    }
}
