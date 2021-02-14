package com.github.fantom.codeowners.languages.github;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.github.fantom.codeowners.languages.github.psi.CodeownersTypes.*;

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

//EOL=\R
//WHITE_SPACE={SPACES}

CRLF="\r" | "\n" | "\r\n"
LINE_WS         = [\ \t\f]
WHITE_SPACE     = ({LINE_WS}*{CRLF}+)+

HEADER          = ###[^\r\n]*
SECTION         = ##[^\r\n]*
COMMENT         = #[^\r\n]*
SLASH           = \/
AT              = @


FIRST_CHARACTER = [^# ]
VALUE=[^@/\s\/]+
SPACES=\s+

%state IN_ENTRY, IN_OWNERS

%%
<YYINITIAL> {
    {WHITE_SPACE}+     { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+         { return CRLF; }
    {HEADER}           { return HEADER; }
    {SECTION}          { return SECTION; }
    {COMMENT}          { return COMMENT; }

    {FIRST_CHARACTER}  { yypushback(1); yybegin(IN_ENTRY); }
}

<IN_ENTRY> {
    {LINE_WS}+          { yybegin(IN_OWNERS); return CRLF; }
    {SLASH}             { yybegin(IN_ENTRY); return SLASH; }

//  "@"                 { return AT; }

    {VALUE}             { yybegin(IN_ENTRY); return VALUE; }
//  {SPACES}           { return SPACES; }
}

<IN_OWNERS> {
    {CRLF}+             { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+          { yybegin(IN_OWNERS); return CRLF; }
    {VALUE}             { yybegin(IN_OWNERS); return VALUE; }
    {SLASH}             { yybegin(IN_OWNERS); return SLASH; }
    {AT}                { yybegin(IN_OWNERS); return AT; }
}

//[^] { return BAD_CHARACTER; }
