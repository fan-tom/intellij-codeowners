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

DESTINATION_BRANCH_PATTERN  = CODEOWNERS.destination_branch_pattern
CREATE_PULL_REQUEST_COMMENT = CODEOWNERS.toplevel.create_pull_request_comment
SUBDIRECTORY_OVERRIDES      = CODEOWNERS.toplevel.subdirectory_overrides
ENABLE                      = enable
DISABLE                     = disable
BRANCH_PATTERN              = [^\s]+


RULE_FIRST_CHARACTER = [^#\ ]
//VALUE=[^@/\s\/]+
VALUE                 = ("\\\["|"\\\]"|"\\\/"|[^\[\]\r\n\/\s])+
NAME_                 = [^#@/\s\/]+
//VALUES_LIST=[^@/]+
SPACES                = \s+

%state IN_BRANCH_PATTERN, IN_TOPLEVEL_CONFIG, IN_ENTRY, IN_OWNERS, IN_TEAM_DEFINITION

%%
<YYINITIAL> {
    {WHITE_SPACE}      { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+         { return WSS; }
    {HEADER}           { return HEADER; }
    {SECTION}          { return SECTION; }
    {COMMENT}          { return COMMENT; }
    {NEGATION}         { return NEGATION; }

    {ATATAT}           { yypushback(yylength()); yybegin(IN_TEAM_DEFINITION); }

    {DESTINATION_BRANCH_PATTERN}         { yybegin(IN_BRANCH_PATTERN); return DESTINATION_BRANCH; }
    {SUBDIRECTORY_OVERRIDES}             { yybegin(IN_TOPLEVEL_CONFIG); return SUBDIRECTORY_OVERRIDES; }
    {CREATE_PULL_REQUEST_COMMENT}        { yybegin(IN_TOPLEVEL_CONFIG); return CREATE_PULL_REQUEST_COMMENT; }

    {RULE_FIRST_CHARACTER}               { yypushback(1); yybegin(IN_ENTRY); }
}

<IN_BRANCH_PATTERN> {
    {CRLF}+                         { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+                      { yybegin(IN_BRANCH_PATTERN); return WSS; }
    {BRANCH_PATTERN}                { yybegin(IN_BRANCH_PATTERN); return BRANCH_PATTERN; }
}

<IN_TOPLEVEL_CONFIG> {
    {CRLF}+                         { yybegin(YYINITIAL); return CRLF; }
    {LINE_WS}+                      { yybegin(IN_TOPLEVEL_CONFIG); return WSS; }
    {ENABLE}                        { yybegin(IN_TOPLEVEL_CONFIG); return ENABLE; }
    {DISABLE}                       { yybegin(IN_TOPLEVEL_CONFIG); return DISABLE; }
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
