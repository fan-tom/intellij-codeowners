// This is a generated file. Not intended for manual editing.
package com.github.fantom.codeowners.language.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.github.fantom.codeowners.language.psi.CodeownersTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class CodeownersParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return codeownersFile(b, l + 1);
  }

  /* ********************************************************** */
  // VALUE
  public static boolean Domain(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Domain")) return false;
    if (!nextTokenIs(b, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VALUE);
    exit_section_(b, m, DOMAIN, r);
    return r;
  }

  /* ********************************************************** */
  // Username '@' Domain
  public static boolean Email(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Email")) return false;
    if (!nextTokenIs(b, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Username(b, l + 1);
    r = r && consumeToken(b, AT);
    r = r && Domain(b, l + 1);
    exit_section_(b, m, EMAIL, r);
    return r;
  }

  /* ********************************************************** */
  // '/' ? <<list_macro VALUE>> '/'
  public static boolean EntryDirectory(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryDirectory")) return false;
    if (!nextTokenIs(b, "<entry directory>", SLASH, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENTRY_DIRECTORY, "<entry directory>");
    r = EntryDirectory_0(b, l + 1);
    r = r && list_macro(b, l + 1, VALUE_parser_);
    r = r && consumeToken(b, SLASH);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '/' ?
  private static boolean EntryDirectory_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryDirectory_0")) return false;
    consumeToken(b, SLASH);
    return true;
  }

  /* ********************************************************** */
  // '/' ? <<list_macro VALUE>>
  public static boolean EntryFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryFile")) return false;
    if (!nextTokenIs(b, "<entry file>", SLASH, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENTRY_FILE, "<entry file>");
    r = EntryFile_0(b, l + 1);
    r = r && list_macro(b, l + 1, VALUE_parser_);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '/' ?
  private static boolean EntryFile_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryFile_0")) return false;
    consumeToken(b, SLASH);
    return true;
  }

  /* ********************************************************** */
  // Email | Team | User
  public static boolean Owner(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Owner")) return false;
    if (!nextTokenIs(b, "<owner>", AT, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OWNER, "<owner>");
    r = Email(b, l + 1);
    if (!r) r = Team(b, l + 1);
    if (!r) r = User(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // Owner (CRLF Owner)*
  public static boolean Owners(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Owners")) return false;
    if (!nextTokenIs(b, "<owners>", AT, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OWNERS, "<owners>");
    r = Owner(b, l + 1);
    r = r && Owners_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (CRLF Owner)*
  private static boolean Owners_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Owners_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!Owners_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "Owners_1", c)) break;
    }
    return true;
  }

  // CRLF Owner
  private static boolean Owners_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Owners_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CRLF);
    r = r && Owner(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (EntryDirectory | EntryFile) /*SPACES*/ CRLF Owners
  public static boolean Pattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern")) return false;
    if (!nextTokenIs(b, "<pattern>", SLASH, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PATTERN, "<pattern>");
    r = Pattern_0(b, l + 1);
    r = r && consumeToken(b, CRLF);
    r = r && Owners(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // EntryDirectory | EntryFile
  private static boolean Pattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern_0")) return false;
    boolean r;
    r = EntryDirectory(b, l + 1);
    if (!r) r = EntryFile(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // '@' TeamName '/' Username
  public static boolean Team(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Team")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AT);
    r = r && TeamName(b, l + 1);
    r = r && consumeToken(b, SLASH);
    r = r && Username(b, l + 1);
    exit_section_(b, m, TEAM, r);
    return r;
  }

  /* ********************************************************** */
  // VALUE
  public static boolean TeamName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TeamName")) return false;
    if (!nextTokenIs(b, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VALUE);
    exit_section_(b, m, TEAM_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // '@' Username
  public static boolean User(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "User")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AT);
    r = r && Username(b, l + 1);
    exit_section_(b, m, USER, r);
    return r;
  }

  /* ********************************************************** */
  // VALUE
  public static boolean Username(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Username")) return false;
    if (!nextTokenIs(b, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VALUE);
    exit_section_(b, m, USERNAME, r);
    return r;
  }

  /* ********************************************************** */
  // item_*
  static boolean codeownersFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "codeownersFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!item_(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "codeownersFile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // HEADER | SECTION | COMMENT | Pattern | CRLF
  static boolean item_(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "item_")) return false;
    boolean r;
    r = consumeToken(b, HEADER);
    if (!r) r = consumeToken(b, SECTION);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = Pattern(b, l + 1);
    if (!r) r = consumeToken(b, CRLF);
    return r;
  }

  /* ********************************************************** */
  // <<p>> + ('/' <<p>> +) *
  static boolean list_macro(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "list_macro")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = list_macro_0(b, l + 1, _p);
    r = r && list_macro_1(b, l + 1, _p);
    exit_section_(b, m, null, r);
    return r;
  }

  // <<p>> +
  private static boolean list_macro_0(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "list_macro_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = _p.parse(b, l);
    while (r) {
      int c = current_position_(b);
      if (!_p.parse(b, l)) break;
      if (!empty_element_parsed_guard_(b, "list_macro_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // ('/' <<p>> +) *
  private static boolean list_macro_1(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "list_macro_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!list_macro_1_0(b, l + 1, _p)) break;
      if (!empty_element_parsed_guard_(b, "list_macro_1", c)) break;
    }
    return true;
  }

  // '/' <<p>> +
  private static boolean list_macro_1_0(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "list_macro_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SLASH);
    r = r && list_macro_1_0_1(b, l + 1, _p);
    exit_section_(b, m, null, r);
    return r;
  }

  // <<p>> +
  private static boolean list_macro_1_0_1(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "list_macro_1_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = _p.parse(b, l);
    while (r) {
      int c = current_position_(b);
      if (!_p.parse(b, l)) break;
      if (!empty_element_parsed_guard_(b, "list_macro_1_0_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  static final Parser VALUE_parser_ = (b, l) -> consumeToken(b, VALUE);
}
