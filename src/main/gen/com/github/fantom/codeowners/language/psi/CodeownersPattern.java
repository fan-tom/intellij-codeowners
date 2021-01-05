// This is a generated file. Not intended for manual editing.
package com.github.fantom.codeowners.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface CodeownersPattern extends PsiElement {

  @Nullable
  CodeownersEntryDirectory getEntryDirectory();

  @Nullable
  CodeownersEntryFile getEntryFile();

  @NotNull
  CodeownersOwners getOwners();

  @NotNull
  PsiElement getCrlf();

}
