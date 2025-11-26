lexer grammar KiteLexer;

// Track interpolation depth to know when RBRACE should pop the mode
@members {
    private int interpolationDepth = 0;
}

// ============================================================================
// DEFAULT MODE - Normal Kite code
// ============================================================================

// Keywords - IaC specific
RESOURCE    : 'resource' ;
COMPONENT   : 'component' ;
SCHEMA      : 'schema' ;
INPUT       : 'input' ;
OUTPUT      : 'output' ;

// Keywords - Control flow
IF          : 'if' ;
ELSE        : 'else' ;
WHILE       : 'while' ;
FOR         : 'for' ;
IN          : 'in' ;
RETURN      : 'return' ;

// Keywords - Declarations
IMPORT      : 'import' ;
FROM        : 'from' ;
FUN         : 'fun' ;
VAR         : 'var' ;
TYPE        : 'type' ;
INIT        : 'init' ;
THIS        : 'this' ;

// Keywords - Types
OBJECT      : 'object' ;
ANY         : 'any' ;

// Literals
TRUE        : 'true' ;
FALSE       : 'false' ;
NULL        : 'null' ;

// Operators - Arithmetic
PLUS        : '+' ;
MINUS       : '-' ;
MULTIPLY    : '*' ;
DIVIDE      : '/' ;
MODULO      : '%' ;
INCREMENT   : '++' ;
DECREMENT   : '--' ;

// Operators - Relational
LT          : '<' ;
GT          : '>' ;
LE          : '<=' ;
GE          : '>=' ;
EQ          : '==' ;
NE          : '!=' ;

// Operators - Logical
AND         : '&&' ;
OR          : '||' ;
NOT         : '!' ;

// Operators - Assignment
ASSIGN      : '=' ;
PLUS_ASSIGN : '+=' ;
MINUS_ASSIGN: '-=' ;
MUL_ASSIGN  : '*=' ;
DIV_ASSIGN  : '/=' ;

// Other operators
ARROW       : '->' ;
RANGE       : '..' ;
DOT         : '.' ;
AT          : '@' ;
UNION       : '|' ;

// Delimiters
LPAREN      : '(' ;
RPAREN      : ')' ;
LBRACE      : '{' ;
// RBRACE pops mode if we're inside a string interpolation (interpolationDepth > 0)
// When closing an interpolation, emit INTERP_END token instead of RBRACE
RBRACE      : '}' { if (interpolationDepth > 0) { interpolationDepth--; setType(INTERP_END); popMode(); } } ;
INTERP_END  : '}' { false }? ;  // Never matches directly - only via setType from RBRACE
LBRACK      : '[' ;
RBRACK      : ']' ;
COMMA       : ',' ;
COLON       : ':' ;
SEMICOLON   : ';' ;

// Literals
NUMBER
    : [0-9]+ ('.' [0-9]+)?
    ;

// Double-quoted string - enter string mode
DQUOTE      : '"' -> pushMode(STRING_MODE) ;

// Single-quoted string (no interpolation)
SINGLE_STRING
    : '\'' SingleStringCharacter* '\''
    ;

fragment
SingleStringCharacter
    : ~['\\\r\n]
    | EscapeSequence
    ;

fragment
EscapeSequence
    : '\\' .
    ;

IDENTIFIER
    : [a-zA-Z_][a-zA-Z0-9_]*
    ;

// Whitespace and Comments
WS
    : [ \t\r]+ -> skip
    ;

NL
    : '\n'
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;

// ============================================================================
// STRING_MODE - Inside double-quoted string
// ============================================================================
mode STRING_MODE;

// End of string
STRING_DQUOTE   : '"' -> popMode ;

// Start of interpolation ${...} - push back to default mode for expression
INTERP_START    : '${' { interpolationDepth++; } -> pushMode(DEFAULT_MODE) ;

// Escaped characters
STRING_ESCAPE   : '\\' . ;

// Regular text (anything except ", \, ${ )
// Note: '$' ~["{] means $ followed by anything except { or " (to not consume closing quote)
STRING_TEXT     : (~["\\$] | '$' ~["{])+ ;

// Lone $ followed by non-{ (handled as text)
STRING_DOLLAR   : '$' ;
