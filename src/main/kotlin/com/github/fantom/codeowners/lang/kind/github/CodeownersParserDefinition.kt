package com.github.fantom.codeowners.lang.kind.github

import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.lang.CodeownersFile
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.github.fantom.codeowners.lang.kind.github.parser.CodeownersParser
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class CodeownersParserDefinition : ParserDefinition {
    override fun createLexer(project: Project) = CodeownersLexerAdapter()

    override fun getWhitespaceTokens() = WHITE_SPACES

    override fun getCommentTokens() = COMMENTS

    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

    override fun createParser(project: Project) = CodeownersParser()

    override fun getFileNodeType() = FILE

    override fun createFile(viewProvider: FileViewProvider) = when (viewProvider.baseLanguage) {
        is CodeownersLanguage -> (viewProvider.baseLanguage as CodeownersLanguage).createFile(viewProvider)
        else -> CodeownersFile(viewProvider, CodeownersFileType.INSTANCE)
    }

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode) = SpaceRequirements.MAY

    override fun createElement(node: ASTNode): PsiElement = CodeownersTypes.Factory.createElement(node)

    companion object {
        /** Whitespaces.  */
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)


        /** Section comment started with ##  */
        val SECTIONS = TokenSet.create(CodeownersTypes.SECTION)

        /** Header comment started with ###  */
        val HEADERS = TokenSet.create(CodeownersTypes.HEADER)

        /** Slashes /  */
        val SLASHES = TokenSet.create(CodeownersTypes.SLASH)

        /** All values - parts of paths  */
        val VALUES = TokenSet.create(CodeownersTypes.VALUE)

        /** Regular comment started with #  */
        val COMMENTS = TokenSet.create(CodeownersTypes.COMMENT)

        /** Element type of the node describing a file in the specified language.  */
        val FILE = IFileElementType(GithubLanguage.INSTANCE)
    }
}
