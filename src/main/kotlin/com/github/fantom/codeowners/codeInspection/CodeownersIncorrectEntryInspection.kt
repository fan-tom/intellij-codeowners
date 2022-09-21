package com.github.fantom.codeowners.codeInspection

/**
 * Inspection tool that checks if entry has correct form in specific according to the specific [ ].
 */
//class CodeownersIncorrectEntryInspection : LocalInspectionTool() {
//    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
//        object : com.github.fantom.codeowners.lang.kind.github.psi.CodeownersVisitor() {
//            override fun visitEntry(entry: com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry) {
//                val regex = entry.regex(false)
//
//                try {
//                    Pattern.compile(regex)
//                } catch (e: PatternSyntaxException) {
//                    holder.registerProblem(
//                        entry,
//                        CodeownersBundle.message("codeInspection.incorrectEntry.message", e.description)
//                    )
//                }
//            }
//        }
//}
