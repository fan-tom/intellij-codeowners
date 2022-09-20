### Terms:
CODEOWNERS file - file that contains codeowners rules
Rule - a record in the CODEOWNERS file, that consists of file path pattern and (optional) owners list
File path pattern - a string that describes paths to files and folders, covered by corresponding rule, usually a glob

Pseudo BNF:
```bnf
File ::= { Rule }
Rule ::= Assign | Reset
Assign ::= Pattern {Owner}
Reset ::= ...
Pattern ::= Glob | ...
Owner ::= Email | Username | Team | ...
```
Ellipsis (`...`) there says that different implementations of CODEOWNERS technology may use different syntaxes
to represent corresponding terms
