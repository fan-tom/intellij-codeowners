package com.github.fantom.codeowners.lang.kind.bitbucket.lexer;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;

import static com.github.fantom.codeowners.lang.kind.bitbucket.psi.CodeownersTypes.*;

%%

%{
  public CodeownersLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class CodeownersLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%debug

//EOL=\R
//WHITE_SPACE={SPACES}

CRLF="\r" | "\n" | "\r\n"
LINE_WS         = [\ \t\f]
WHITE_SPACE     = ({LINE_WS}*{CRLF}+)+

HEADER          = ###[^\r\n]*
SECTION         = ##[^\r\n]*
COMMENT         = #[^\r\n]*
NEGATION        = \!
SLASH           = \/
AT              = @
QUOTE           = \"


ENTRY_FIRST_CHARACTER = [^#@ ]
VALUE=[^@/\s\/]+
//VALUES_LIST=[^@/]+
SPACES=\s+

%state IN_ENTRY, IN_OWNERS, IN_TEAM_DEFINITION

%%
<YYINITIAL> {
    {WHITE_SPACE}      { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+         { return CRLF; }
    {HEADER}           { return HEADER; }
    {SECTION}          { return SECTION; }
    {COMMENT}          { return COMMENT; }
    {NEGATION}         { return NEGATION; }

    {AT}               { yypushback(1); yybegin(IN_TEAM_DEFINITION); }

    {ENTRY_FIRST_CHARACTER}  { yypushback(1); yybegin(IN_ENTRY); }
}

<IN_TEAM_DEFINITION> {
    {AT}                { yybegin(IN_TEAM_DEFINITION); return AT; }
    {VALUE}             { yybegin(IN_TEAM_DEFINITION); return VALUE; }
//    {QUOTED_VALUE}      { return QUOTED_VALUE; }
    {LINE_WS}+          { yybegin(IN_OWNERS); return CRLF; }
    {CRLF}+             { yybegin(YYINITIAL); return CRLF; }
}

<IN_ENTRY> {
//    {QUOTE}             { yybegin(IN_WS_ENTRY); return QUOTE; }
    {LINE_WS}+          { yybegin(IN_OWNERS); return CRLF; }
    {SLASH}             { yybegin(IN_ENTRY); return SLASH; }

//  "@"                 { return AT; }

    {VALUE}             { yybegin(IN_ENTRY); return VALUE; }
//  {SPACES}           { return SPACES; }
}

//<IN_WS_ENTRY> {
//    {LINE_WS}+          { yybegin(IN_OWNERS); return CRLF; }
//    {SLASH}             { yybegin(IN_ENTRY); return SLASH; }
//    {VALUES_LIST}       { yybegin(IN_WS_ENTRY); return VALUES_LIST; }
//}

<IN_OWNERS> {
    {CRLF}+             { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+          { yybegin(IN_OWNERS); return CRLF; }
    {VALUE}             { yybegin(IN_OWNERS); return VALUE; }
//    {SLASH}             { return SLASH; }
    {AT}                { yybegin(IN_OWNERS); return AT; }
}

//[^] { return BAD_CHARACTER; }
