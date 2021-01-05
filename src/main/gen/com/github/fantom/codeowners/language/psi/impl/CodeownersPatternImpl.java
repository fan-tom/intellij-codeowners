// This is a generated file. Not intended for manual editing.
package com.github.fantom.codeowners.language.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.github.fantom.codeowners.language.psi.CodeownersTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.github.fantom.codeowners.language.psi.*;

public class CodeownersPatternImpl extends ASTWrapperPsiElement implements CodeownersPattern {

  public CodeownersPatternImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CodeownersVisitor visitor) {
    visitor.visitPattern(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CodeownersVisitor) accept((CodeownersVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public CodeownersEntryDirectory getEntryDirectory() {
    return findChildByClass(CodeownersEntryDirectory.class);
  }

  @Override
  @Nullable
  public CodeownersEntryFile getEntryFile() {
    return findChildByClass(CodeownersEntryFile.class);
  }

  @Override
  @NotNull
  public CodeownersOwners getOwners() {
    return findNotNullChildByClass(CodeownersOwners.class);
  }

  @Override
  @NotNull
  public PsiElement getCrlf() {
    return findNotNullChildByType(CRLF);
  }

}
