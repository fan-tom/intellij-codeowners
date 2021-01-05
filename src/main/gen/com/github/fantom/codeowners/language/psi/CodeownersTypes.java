// This is a generated file. Not intended for manual editing.
package com.github.fantom.codeowners.language.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.github.fantom.codeowners.language.psi.impl.*;

public interface CodeownersTypes {

  IElementType DOMAIN = new CodeownersElementType("DOMAIN");
  IElementType EMAIL = new CodeownersElementType("EMAIL");
  IElementType ENTRY_DIRECTORY = new CodeownersElementType("ENTRY_DIRECTORY");
  IElementType ENTRY_FILE = new CodeownersElementType("ENTRY_FILE");
  IElementType OWNER = new CodeownersElementType("OWNER");
  IElementType OWNERS = new CodeownersElementType("OWNERS");
  IElementType PATTERN = new CodeownersElementType("PATTERN");
  IElementType TEAM = new CodeownersElementType("TEAM");
  IElementType TEAM_NAME = new CodeownersElementType("TEAM_NAME");
  IElementType USER = new CodeownersElementType("USER");
  IElementType USERNAME = new CodeownersElementType("USERNAME");

  IElementType AT = new CodeownersTokenType("@");
  IElementType COMMENT = new CodeownersTokenType("COMMENT");
  IElementType CRLF = new CodeownersTokenType("CRLF");
  IElementType HEADER = new CodeownersTokenType("HEADER");
  IElementType SECTION = new CodeownersTokenType("SECTION");
  IElementType SLASH = new CodeownersTokenType("/");
  IElementType VALUE = new CodeownersTokenType("VALUE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == DOMAIN) {
        return new CodeownersDomainImpl(node);
      }
      else if (type == EMAIL) {
        return new CodeownersEmailImpl(node);
      }
      else if (type == ENTRY_DIRECTORY) {
        return new CodeownersEntryDirectoryImpl(node);
      }
      else if (type == ENTRY_FILE) {
        return new CodeownersEntryFileImpl(node);
      }
      else if (type == OWNER) {
        return new CodeownersOwnerImpl(node);
      }
      else if (type == OWNERS) {
        return new CodeownersOwnersImpl(node);
      }
      else if (type == PATTERN) {
        return new CodeownersPatternImpl(node);
      }
      else if (type == TEAM) {
        return new CodeownersTeamImpl(node);
      }
      else if (type == TEAM_NAME) {
        return new CodeownersTeamNameImpl(node);
      }
      else if (type == USER) {
        return new CodeownersUserImpl(node);
      }
      else if (type == USERNAME) {
        return new CodeownersUsernameImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
