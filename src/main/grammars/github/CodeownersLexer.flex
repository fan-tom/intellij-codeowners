package com.github.fantom.codeowners.lang.kind.github.lexer;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;

import static com.github.fantom.codeowners.lang.kind.github.psi.CodeownersTypes.*;

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

CRLF            = "\r" | "\n" | "\r\n"
LINE_WS         = [\ \t\f]
WHITE_SPACE     = ({LINE_WS}*{CRLF}+)+

HEADER          = ###[^\r\n]*
SECTION         = ##[^\r\n]*
COMMENT         = #[^\r\n]*
SLASH           = \/
AT              = @


FIRST_CHARACTER = [^#\s]
VALUE           = [^@\s/]+
PATHNAME        = ([^\s/]|\\\s)+

%state IN_PATTERN, IN_OWNERS

%%
<YYINITIAL> {
    {WHITE_SPACE}+     { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+         { return CRLF; }
    {HEADER}           { return HEADER; }
    {SECTION}          { return SECTION; }
    {COMMENT}          { return COMMENT; }

    {FIRST_CHARACTER}  { yypushback(1); yybegin(IN_PATTERN); }
}

<IN_PATTERN> {
    {LINE_WS}+          { yybegin(IN_OWNERS); return SPACES; }
    {SLASH}             { yybegin(IN_PATTERN); return SLASH; }

    {PATHNAME}          { yybegin(IN_PATTERN); return PATHNAME; }
    {CRLF}+             { yybegin(YYINITIAL); return CRLF; }
}

<IN_OWNERS> {
    {CRLF}+             { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+          { yybegin(IN_OWNERS); return SPACES; }
    {VALUE}             { yybegin(IN_OWNERS); return VALUE; }
    {SLASH}             { yybegin(IN_OWNERS); return SLASH; }
    {AT}                { yybegin(IN_OWNERS); return AT; }
}

//[^] { return BAD_CHARACTER; }
