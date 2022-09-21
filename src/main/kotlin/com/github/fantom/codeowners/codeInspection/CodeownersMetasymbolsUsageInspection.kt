package com.github.fantom.codeowners.codeInspection

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.lang.CodeownersRuleBase
import com.github.fantom.codeowners.lang.CodeownersVisitor
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor

/**
 * Inspection tool that checks incorrect usage of metasymbols:
 * - two double stars in a row
 * - doublestars at the beginning of the pattern if there are no other slashes
 */
class CodeownersMetasymbolsUsageInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
//        val matcher = holder.project.service<CodeownersMatcher>()
        val inspectionManager = InspectionManager.getInstance(holder.project)
        val visitor = object : CodeownersVisitor() {
            override fun visitRule(rule: CodeownersRuleBase<*, *>) {
                val pattern = rule.pattern
                val text = pattern.text
                if (text.startsWith("**/")) {
                    when (text.indexOf('/', 3)) {
                        -1, text.indices.last ->
                            holder.registerProblem(
                                pattern.findElementAt(1)!!,
                                CodeownersBundle.message("codeInspection.metasymbolsUsage.leadingDoubleStar"),
                            )
                    }
                }
                val doubleStars = "**/**"
                val idx = text.indexOf(doubleStars)
                if (idx != -1) {
                    holder.registerProblem(
                        inspectionManager.createProblemDescriptor(
                            pattern,
                            TextRange(idx, idx + doubleStars.length),
                            CodeownersBundle.message("codeInspection.metasymbolsUsage.consecutiveDoubleStars"),
                            ProblemHighlightType.WEAK_WARNING,
                            isOnTheFly
                        )
                    )
                }
            }
        }

        return (holder.file.fileType as CodeownersFileType).codeownersLanguage.getVisitor(visitor)!!
    }
}
