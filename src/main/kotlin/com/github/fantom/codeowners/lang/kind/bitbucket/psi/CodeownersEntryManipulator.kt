package com.github.fantom.codeowners.lang.kind.bitbucket.psi

/**
 * Entry manipulator.
 */
//class CodeownersPatternManipulator : AbstractElementManipulator<com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry>() {
//
//    @Throws(IncorrectOperationException::class)
//    override fun handleContentChange(
//        entry: com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry,
//        range: TextRange,
//        newContent: String
//    ): com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry {
//        val language = entry.language as? CodeownersLanguage ?: return entry
//        val fileType = (language.associatedFileType as CodeownersFileType)
//        val file = PsiFileFactory.getInstance(entry.project)
//            .createFileFromText(language.filename, fileType, range.replace(entry.text, newContent))
//
//        return when (
//            val newEntry = PsiTreeUtil.findChildOfType(
//                file,
//                com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry::class.java
//            )
//        ) {
//            null -> entry
//            else -> entry.replace(newEntry) as com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry
//        }
//    }
//
////    override fun getRangeInElement(element: CodeownersEntry) = element.negation?.run {
////        TextRange.create(startOffsetInParent + textLength, element.textLength)
////    } ?: super.getRangeInElement(element)
//}
