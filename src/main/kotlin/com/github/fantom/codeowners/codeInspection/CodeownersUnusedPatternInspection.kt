package com.github.fantom.codeowners.codeInspection

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.lang.CodeownersRuleBase
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
class CodeownersUnusedPatternInspection : LocalInspectionTool() {

    /**
     * Checks if entries are related to any file.
     *
     * @param holder     where visitor will register problems found.
     * @param isOnTheFly true if inspection was run in non-batch mode
     * @return not-null visitor for this inspection
     */
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val visitor = object : CodeownersVisitor() {
            override fun visitRule(rule: CodeownersRuleBase<*, *>) {
                val pattern = rule.pattern
                val lastReference = pattern.references.lastOrNull {
                    it is FileReferenceOwner
                } ?: return
                val hasNoTargets = (lastReference as PsiPolyVariantReference).multiResolve(false).isEmpty()
                if (hasNoTargets) {
                    holder.registerProblem(
                        pattern,
                        CodeownersBundle.message("codeInspection.unusedPattern.message"),
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                        CodeownersRemoveRuleFix(rule)
                    )
                }
            }
        }

        return (holder.file.fileType as CodeownersFileType).codeownersLanguage.getVisitor(visitor)!!
    }
}
