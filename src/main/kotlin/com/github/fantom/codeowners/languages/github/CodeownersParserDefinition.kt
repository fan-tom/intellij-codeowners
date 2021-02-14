package com.github.fantom.codeowners.languages.github

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.psi.tree.TokenSet
import com.github.fantom.codeowners.languages.github.parser.CodeownersParser
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.FileViewProvider
import com.github.fantom.codeowners.languages.github.psi.CodeownersFile
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.github.fantom.codeowners.languages.github.psi.CodeownersTypes
import com.github.fantom.codeowners.CodeownersLanguage

class CodeownersParserDefinition : ParserDefinition {
    override fun createLexer(project: Project) = CodeownersLexerAdapter()

    override fun getWhitespaceTokens() = WHITE_SPACES

    override fun getCommentTokens() = COMMENTS

    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

    override fun createParser(project: Project) = CodeownersParser()

    override fun getFileNodeType() = FILE

    override fun createFile(viewProvider: FileViewProvider) = CodeownersFile(viewProvider)

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode) = SpaceRequirements.MAY

    override fun createElement(node: ASTNode): PsiElement = CodeownersTypes.Factory.createElement(node)

    companion object {
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        val COMMENTS = TokenSet.create(CodeownersTypes.COMMENT)
        val FILE = IFileElementType(CodeownersLanguage.INSTANCE)
    }
}
