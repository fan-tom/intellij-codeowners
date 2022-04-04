package com.github.fantom.codeowners.lang.kind.bitbucket.psi.impl

import com.github.fantom.codeowners.lang.CodeownersElementImpl
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersTeamDefinition
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import javax.swing.Icon

abstract class CodeownersTeamDefinitionExtImpl(node: ASTNode) : CodeownersElementImpl(node), CodeownersTeamDefinition {
    override fun getName(): String = teamName.text

    override fun getPresentation(): ItemPresentation? {
        return object : ItemPresentation {
            override fun getPresentableText(): String = name
            override fun getIcon(unused: Boolean): Icon? = null
        }
    }
}
