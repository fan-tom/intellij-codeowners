package com.github.fantom.codeowners.highlighter

import com.github.fantom.codeowners.highlighter.CodeownersHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * [SyntaxHighlighterFactory] class definition.
 */
class CodeownersHighlighterFactory : SyntaxHighlighterFactory() {

    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?) = CodeownersHighlighter(virtualFile)
}
