package com.github.fantom.codeowners.lang.kind.bitbucket.psi.impl;

import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersTeam;
import com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersTypes;
import com.intellij.psi.PsiElement;

public class CodeownersPsiImplUtilJava {
    public static PsiElement getNameIdentifier(CodeownersTeam element) {
        var node = element.getNode().findChildByType(CodeownersTypes.TEAM_NAME);
        if (node != null) {
            return node.getPsi();
        }
        return null;
    }

    public static String getName(CodeownersTeam element) {
        return element.getText();
    }

    public static PsiElement setName(CodeownersTeam element, String newName) {
//            val teamNameNode = element.node.findChildByType(CodeownersTypes.TEAM_NAME)
//            if (teamNameNode != null) {
//                val property = SimpleElementFactory.createProperty(element.project, newName)
//                val newTeamNameNode = property.getFirstChild().getNode()
//                element.node.replaceChild(teamNameNode, newTeamNameNode)
//            }
        return element;
    }
}
