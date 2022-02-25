package com.github.fantom.codeowners.languages.kind.github.

import com.intellij.psi.tree.IElementType;
import static com.github.fantom.codeowners.lang.kind.github.psi.CodeownersTypes.*;
%%

%public
%class _CodeownersLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

CRLF            = "\r"|"\n"|"\r\n"
LINE_WS         = [\ \t\f]
WHITE_SPACE     = ({LINE_WS}*{CRLF}+)+

HEADER          = ###[^\r\n]*
SECTION         = ##[^\r\n]*
COMMENT         = #[^\r\n]*
SLASH           = \/

FIRST_CHARACTER = [^!# ]
VALUE           = ("\\\["|"\\\]"|"\\\/"|[^\[\]\r\n\/])+

%state IN_ENTRY, IN_SYNTAX

%%
<YYINITIAL> {
    {WHITE_SPACE}+      { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+          { return CRLF; }
    {HEADER}            { return HEADER; }
    {SECTION}           { return SECTION; }
    {COMMENT}           { return COMMENT; }

    {FIRST_CHARACTER}   { yypushback(1); yybegin(IN_ENTRY); }
}

<IN_ENTRY> {
    {WHITE_SPACE}+      { yybegin(YYINITIAL); return CRLF; }
    {SLASH}             { yybegin(IN_ENTRY); return SLASH; }

    {VALUE}             { yybegin(IN_ENTRY); return VALUE; }
}
