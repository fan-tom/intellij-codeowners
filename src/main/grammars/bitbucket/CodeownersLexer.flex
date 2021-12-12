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

CRLF            = "\r" | "\n" | "\r\n"
LINE_WS         = [\ \t\f]
WHITE_SPACE     = ({LINE_WS}*{CRLF}+)+

HEADER          = ###[^\r\n]*
SECTION         = ##[^\r\n]*
COMMENT         = #[^\r\n]*
NEGATION        = \!
SLASH           = \/
ATATAT          = @@@
AT              = @
QUOTE           = \"
DOT             = "."

CODEOWNERS_DOT              = CODEOWNERS\.
CODEOWNERS                  = CODEOWNERS
DESTINATION_BRANCH_PATTERN  = destination_branch_pattern
TOP_LEVEL                   = toplevel
SUBDIRECTORY_OVERRIDES      = subdirectory_overrides
CREATE_PULL_REQUEST_COMMENT = create_pull_request_comment
ENABLE                      = enable
DISABLE                     = disable
BRANCH_PATTERN              = [^\s]+


ENTRY_FIRST_CHARACTER = [^#\ ]
//VALUE=[^@/\s\/]+
VALUE                 = ("\\\["|"\\\]"|"\\\/"|[^\[\]\r\n\/\s])+
NAME_                 = [^#@/\s\/]+
//VALUES_LIST=[^@/]+
SPACES                = \s+

%state IN_CONFIGURATION_LINE, IN_BRANCH_PATTERN, IN_ENABLE_DISABLE, IN_ENTRY, IN_OWNERS, IN_TEAM_DEFINITION

%%
<YYINITIAL> {
    {WHITE_SPACE}      { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+         { return WSS; }
    {HEADER}           { return HEADER; }
    {SECTION}          { return SECTION; }
    {COMMENT}          { return COMMENT; }
    {NEGATION}         { return NEGATION; }

    {ATATAT}           { yypushback(yylength()); yybegin(IN_TEAM_DEFINITION); }

    {CODEOWNERS_DOT}         { yypushback(yylength()); yybegin(IN_CONFIGURATION_LINE); }
    {ENTRY_FIRST_CHARACTER}  { yypushback(1); yybegin(IN_ENTRY); }
}

<IN_CONFIGURATION_LINE> {
    {CRLF}+                         { yybegin(YYINITIAL); return CRLF; }
    {CODEOWNERS}                    { yybegin(IN_CONFIGURATION_LINE); return CODEOWNERS; }
    {DOT}                           { yybegin(IN_CONFIGURATION_LINE); return DOT; }

    {DESTINATION_BRANCH_PATTERN}    { yybegin(IN_BRANCH_PATTERN); return DESTINATION_BRANCH_PATTERN; }
    {TOP_LEVEL}                     { yybegin(IN_CONFIGURATION_LINE); return TOPLEVEL; }
    {SUBDIRECTORY_OVERRIDES}        { yybegin(IN_ENABLE_DISABLE); return SUBDIRECTORY_OVERRIDES; }
    {CREATE_PULL_REQUEST_COMMENT}   { yybegin(IN_ENABLE_DISABLE); return CREATE_PULL_REQUEST_COMMENT; }
}

<IN_BRANCH_PATTERN> {
    {CRLF}+                         { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+                      { yybegin(IN_BRANCH_PATTERN); return WSS; }
    {BRANCH_PATTERN}                { yybegin(IN_BRANCH_PATTERN); return BRANCH_PATTERN; }
}

<IN_ENABLE_DISABLE> {
    {CRLF}+                         { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+                      { yybegin(IN_ENABLE_DISABLE); return WSS; }
    {ENABLE}                        { yybegin(IN_ENABLE_DISABLE); return ENABLE; }
    {DISABLE}                       { yybegin(IN_ENABLE_DISABLE); return DISABLE; }
}

<IN_TEAM_DEFINITION> {
    {AT}                { yybegin(IN_TEAM_DEFINITION); return AT; }
    {NAME_}             { yybegin(IN_TEAM_DEFINITION); return NAME_; }
//    {QUOTED_VALUE}      { return QUOTED_VALUE; }
    {LINE_WS}+          { yybegin(IN_OWNERS); return WSS; }
    {CRLF}+             { yybegin(YYINITIAL); return CRLF; }
}

<IN_ENTRY> {
//    {QUOTE}             { yybegin(IN_WS_ENTRY); return QUOTE; }
    {CRLF}+             { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+          { yybegin(IN_OWNERS); return WSS; }
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
    {COMMENT}           { yybegin(YYINITIAL); return COMMENT; }
    {CRLF}+             { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+          { yybegin(IN_OWNERS); return WSS; }
    {NAME_}             { yybegin(IN_OWNERS); return NAME_; }
//    {SLASH}             { return SLASH; }
    {AT}                { yybegin(IN_OWNERS); return AT; }
}

//[^] { return BAD_CHARACTER; }
