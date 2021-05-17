package com.github.fantom.codeowners.lang.kind.github.highlighter

import com.github.fantom.codeowners.highlighter.CodeownersHighlighterColors
import com.github.fantom.codeowners.lang.kind.github.CodeownersLexerAdapter
import com.github.fantom.codeowners.lang.kind.github.CodeownersParserDefinition
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType

/**
 * Syntax highlighter definition.
 */
class CodeownersHighlighter(private val virtualFile: VirtualFile?) : SyntaxHighlighterBase() {

    companion object {
        private val ATTRIBUTES = mutableMapOf<IElementType, TextAttributesKey>()

        init {
            fillMap(ATTRIBUTES, CodeownersParserDefinition.COMMENTS, CodeownersHighlighterColors.COMMENT)
            fillMap(ATTRIBUTES, CodeownersParserDefinition.SECTIONS, CodeownersHighlighterColors.SECTION)
            fillMap(ATTRIBUTES, CodeownersParserDefinition.HEADERS, CodeownersHighlighterColors.HEADER)
            fillMap(ATTRIBUTES, CodeownersParserDefinition.SLASHES, CodeownersHighlighterColors.SLASH)
            fillMap(ATTRIBUTES, CodeownersParserDefinition.VALUES, CodeownersHighlighterColors.VALUE)
        }
    }

    override fun getHighlightingLexer() = CodeownersLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = pack(ATTRIBUTES[tokenType])
}
