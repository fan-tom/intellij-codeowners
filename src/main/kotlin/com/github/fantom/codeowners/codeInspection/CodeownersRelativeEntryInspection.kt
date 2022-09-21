package com.github.fantom.codeowners.codeInspection

//import com.github.fantom.codeowners.CodeownersBundle
//import com.github.fantom.codeowners.lang.CodeownersFile
//import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry
//import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersVisitor
//import com.intellij.codeInspection.InspectionManager
//import com.intellij.codeInspection.LocalInspectionTool
//import com.intellij.codeInspection.ProblemDescriptor
//import com.intellij.codeInspection.ProblemsHolder
//import com.intellij.psi.PsiFile
//
///**
// * Inspection tool that checks if entry is relative.
// */
//class CodeownersRelativeEntryInspection : LocalInspectionTool() {
//
//    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
//        if (file !is CodeownersFile) {
//            return null
//        }
//
//        val problemsHolder = ProblemsHolder(manager, file, isOnTheFly)
//        file.acceptChildren(
//            object : CodeownersVisitor() {
//                override fun visitEntry(entry: CodeownersEntry) {
//                    val path = entry.text.replace("\\\\(.)".toRegex(), "$1")
//                    if (path.contains("./")) {
//                        problemsHolder.registerProblem(
//                            entry,
//                            CodeownersBundle.message("codeInspection.relativeEntry.message"),
//                            CodeownersRelativeEntryFix(entry)
//                        )
//                    }
//                    super.visitEntry(entry)
//                }
//            }
//        )
//        return problemsHolder.resultsArray
//    }
//
//    override fun runForWholeFile() = true
//}
