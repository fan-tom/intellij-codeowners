package com.github.fantom.codeowners.codeInspection

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.languages.github.psi.CodeownersEntry
import com.github.fantom.codeowners.languages.github.psi.CodeownersVisitor
import com.github.fantom.codeowners.util.Glob
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Inspection tool that checks if entry has correct form in specific according to the specific [ ].
 */
class CodeownersIncorrectEntryInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = object : CodeownersVisitor() {
        override fun visitEntry(entry: CodeownersEntry) {
            val regex = Glob.createRegex(entry.text, false)

            try {
                Pattern.compile(regex)
            } catch (e: PatternSyntaxException) {
                holder.registerProblem(entry, CodeownersBundle.message("codeInspection.incorrectEntry.message", e.description))
            }
        }
    }
}
