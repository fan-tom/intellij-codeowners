package com.github.fantom.codeowners.lang

import com.github.fantom.codeowners.CodeownersException
import com.github.fantom.codeowners.OwnersReference
import com.intellij.psi.FileViewProvider
import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.indexing.PatternString
import com.intellij.lang.Language
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.PsiFileImpl

class CodeownersFile(viewProvider: FileViewProvider, private val fileType: CodeownersFileType) : PsiFileImpl(viewProvider) {

    private val language = findLanguage(fileType.language, viewProvider) as CodeownersLanguage

    companion object {
        /**
         * Searches for the matching language in [FileViewProvider].
         *
         * @param baseLanguage language to look for
         * @param viewProvider current [FileViewProvider]
         * @return matched [Language]
         */
        private fun findLanguage(baseLanguage: Language, viewProvider: FileViewProvider): Language = viewProvider.languages.run {
            find { it.isKindOf(baseLanguage) }?.let { return it }
            find { it is CodeownersLanguage }?.let { return it }
            throw AssertionError("Language $baseLanguage doesn't participate in view provider $viewProvider: $this")
        }
    }

    init {
        LanguageParserDefinitions.INSTANCE.forLanguage(language).apply {
            init(fileNodeType, fileNodeType)
        } ?: throw CodeownersException("PsiFileBase: language.getParserDefinition() returned null for: $language")
    }

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
    }

    override fun getFileType() = fileType

    override fun getLanguage() = language

    override fun toString() = fileType.name

    fun getPatternsList(): List<Pair<PatternString, OwnersReference>> {
        val items = mutableListOf<Pair<PatternString, OwnersReference>>()
        language.getPatternsVisitor(items)?.let { acceptChildren(it) }
        return items
    }
}
