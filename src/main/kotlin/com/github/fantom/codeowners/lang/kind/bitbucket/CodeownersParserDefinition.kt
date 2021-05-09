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
import org.apache.log4j.Level

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
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        val COMMENTS = TokenSet.create(CodeownersTypes.COMMENT)
        val FILE = IFileElementType(BitbucketLanguage.INSTANCE)
    }
}
