package com.github.fantom.codeowners.lang.kind.bitbucket

import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.lang.CodeownersFile
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.github.fantom.codeowners.lang.kind.bitbucket.parser.CodeownersParser
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.openapi.diagnostic.Logger
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
        else -> {
            LOGGER.trace("Creating generic codeowners file")
            CodeownersFile(viewProvider, CodeownersFileType.INSTANCE)
        }
    }

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode) = SpaceRequirements.MAY

    override fun createElement(node: ASTNode): PsiElement = CodeownersTypes.Factory.createElement(node)

    companion object {
        private val LOGGER = Logger.getInstance(CodeownersParserDefinition::class.java)
        /** Whitespaces.  */
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)

        /** Section comment started with ##  */
        val SECTIONS = TokenSet.create(CodeownersTypes.SECTION)

        /** Header comment started with ###  */
        val HEADERS = TokenSet.create(CodeownersTypes.HEADER)

        /** Negation element - ! in the beginning of the entry  */
        val NEGATIONS = TokenSet.create(CodeownersTypes.NEGATION)

        /** Brackets []  */
//        val BRACKETS = TokenSet.create(CodeownersTypes.BRACKET_LEFT, CodeownersTypes.BRACKET_RIGHT)

        /** Slashes /  */
        val SLASHES = TokenSet.create(CodeownersTypes.SLASH)

        /** All values - parts of paths  */
        val VALUES = TokenSet.create(CodeownersTypes.VALUE)

        /** All values - parts of paths  */
        val NAMES = TokenSet.create(CodeownersTypes.NAME_)

        val CONFIG_NAMES = TokenSet.create(
            CodeownersTypes.DESTINATION_BRANCH,
            CodeownersTypes.CREATE_PULL_REQUEST_COMMENT,
            CodeownersTypes.SUBDIRECTORY_OVERRIDES,
        )

        val CONFIG_VALUES = TokenSet.create(
            CodeownersTypes.BRANCH_PATTERN,
            CodeownersTypes.ENABLE,
            CodeownersTypes.DISABLE,
        )

        /** Regular comment started with #  */
        val COMMENTS = TokenSet.create(CodeownersTypes.COMMENT)

        /** Element type of the node describing a file in the specified language.  */
        val FILE = IFileElementType(BitbucketLanguage.INSTANCE)
    }
}
