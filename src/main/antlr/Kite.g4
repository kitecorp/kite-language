grammar Kite;

// ============================================================================
// PARSER RULES (lowercase)
// ============================================================================

// Entry point
program
    : statementTerminator* statementList? EOF
    ;

statementList
    : nonEmptyStatement (statementTerminator+ nonEmptyStatement)* statementTerminator*
    ;

statementTerminator
    : NL
    | ';'
    ;

nonEmptyStatement
    : declaration
    | ifStatement
    | initStatement
    | returnStatement
    | iterationStatement
    | blockExpression
    | expressionStatement
    ;
statement
    : nonEmptyStatement
    | emptyStatement
    ;

emptyStatement
    : NL
    ;

// Declarations
declaration
    : decoratorList? NL* functionDeclaration
    | decoratorList? NL* typeDeclaration
    | decoratorList? NL* schemaDeclaration
    | decoratorList? NL* resourceDeclaration
    | decoratorList? NL* componentDeclaration
    | decoratorList? NL* inputDeclaration
    | decoratorList? NL* outputDeclaration
    | decoratorList? NL* varDeclaration
    ;

functionDeclaration
    : FUN identifier '(' parameterList? ')' typeIdentifier? blockExpression
    ;

typeDeclaration
    : TYPE identifier '=' typeParams
    ;

typeParams
    : unionTypeParam ('|' unionTypeParam)*
    ;

unionTypeParam
    : literal
    | objectExpression
    | arrayExpression
    | typeKeyword
    | identifier
    ;

typeKeyword
    : OBJECT
    | ANY
    ;

schemaDeclaration
    : SCHEMA identifier '{' NL* (schemaProperty NL*)* '}'
    ;

schemaProperty
    : decoratorList? typeIdentifier identifier propertyInitializer?
    ;

propertyInitializer
    : '=' expression
    ;

resourceDeclaration
    : RESOURCE typeIdentifier resourceName blockExpression
    ;

resourceName
    : identifier
    | callMemberExpression
    | STRING
    ;

componentDeclaration
    : COMPONENT componentType identifier? blockExpression
    ;

componentType
    : typeIdentifier
    ;

inputDeclaration
    : INPUT typeIdentifier identifier ('=' expression)?
    ;

outputDeclaration
    : OUTPUT typeIdentifier identifier ('=' expression)?
    ;

varDeclaration
    : VAR varDeclarationList
    ;

varDeclarationList
    : varDeclarator (',' varDeclarator)*
    ;

varDeclarator
    : typeIdentifier? identifier varInitializer?
    ;

varInitializer
    : ('=' | '+=') expression
    ;

// Statements
ifStatement
    : IF '(' expression ')' NL* blockExpression elseStatement?
    | IF expression NL* blockExpression elseStatement?
    ;

elseStatement
    : ELSE NL* blockExpression
    ;

iterationStatement
    : whileStatement
    | forStatement
    ;

whileStatement
    : WHILE '(' expression ')' NL* blockExpression
    | WHILE expression NL* blockExpression
    ;

rangeExpression
    : NUMBER '..' NUMBER
    ;

initStatement
    : INIT '(' parameterList? ')' blockExpression
    ;

returnStatement
    : RETURN expression?
    ;

expressionStatement
    : expression
    ;

// Decorators/Annotations
decoratorList
    : decorator (NL* decorator)*
    ;

decorator
    : '@' identifier ('(' NL* decoratorArgs NL* ')')?
    ;

decoratorArgs
    : decoratorArg (NL* ',' NL* decoratorArg)* (NL* ',')? NL*
    | namedArg (NL* ',' NL* namedArg)* (NL* ',')? NL*
    ;

namedArg
    : identifier '=' expression
    ;

decoratorArg
    : arrayExpression
    | objectExpression
    | callMemberExpression
    | identifier
    | literal
    | '-' NUMBER
    ;

// Expressions (precedence from lowest to highest)
expression
    : objectExpression
    | arrayExpression
    | assignmentExpression
    ;

assignmentExpression
    : orExpression (('=' | '+=') expression)?
    ;

orExpression
    : andExpression ('||' andExpression)*
    ;

andExpression
    : equalityExpression ('&&' equalityExpression)*
    ;

equalityExpression
    : relationalExpression (('==' | '!=') relationalExpression)*
    ;

relationalExpression
    : additiveExpression (('<' | '>' | '<=' | '>=') additiveExpression)*
    ;

additiveExpression
    : multiplicativeExpression (('+' | '-') multiplicativeExpression)*
    ;

multiplicativeExpression
    : unaryExpression (('*' | '/' | '%') unaryExpression)*
    ;

unaryExpression
    : ('-' | '++' | '--' | '!') unaryExpression
    | leftHandSideExpression
    ;

leftHandSideExpression
    : callMemberExpression
    ;

callMemberExpression
    : primaryExpression (callOrMemberAccess)*
    ;

callOrMemberAccess
    : '(' argumentList? ')'
    | '.' identifier
    | '[' expression ']'
    ;

primaryExpression
    : '(' expression ')'
    | lambdaExpression
    | literal
    | identifier
    | thisExpression
    ;

thisExpression
    : THIS
    ;

lambdaExpression
    : '(' parameterList? ')' typeIdentifier? '->' lambdaBody
    ;

lambdaBody
    : blockExpression
    | expression
    ;

blockExpression
    : '{' statementTerminator* statementList? statementTerminator* '}'
    ;

objectExpression
    : objectDeclaration
    ;

objectDeclaration
    : OBJECT '(' ('{' NL* objectPropertyList? NL* '}')? ')'  // object() or object({ key: value })
    | '{' NL* objectPropertyList? NL* '}'                     // { key: value }
    ;

objectPropertyList
    : objectProperty (NL* ',' NL* objectProperty)* (NL* ',')? NL*
    ;

objectProperty
    : objectKey objectInitializer?
    ;

objectKey
    : STRING
    | IDENTIFIER
    ;

objectInitializer
    : ':' expression
    ;

arrayExpression
    : '[' FOR identifier (',' identifier)? IN (rangeExpression | arrayExpression | identifier) ':' compactBody ']'  // Form 1: [for ...: body]
    | '[' FOR identifier (',' identifier)? IN (rangeExpression | arrayExpression | identifier) ']' NL* forBody      // Form 2: [for ...] body
    | '[' arrayItems? ']'                                                                                            // Literal
    ;

compactBody
    : ifStatement
    | expression
    ;

forStatement
    : FOR identifier (',' identifier)? IN (rangeExpression | arrayExpression | identifier) NL* forBody             // Form 2: for ... body
    ;
forBody
    : blockExpression
    | resourceDeclaration
    | ifStatement
    | expressionStatement
    | emptyStatement
    ;
arrayItems
    : arrayItem (',' arrayItem)*
    ;

arrayItem
    : callMemberExpression
    | identifier
    | objectExpression
    | typeKeyword       // Add this
    | literal
    ;

// Type System
typeIdentifier
    : functionType ('[' NUMBER? ']')*
    | (complexTypeIdentifier | OBJECT | ANY) ('[' NUMBER? ']')*
    ;
functionType
    : '(' functionTypeParams? ')' '->' typeIdentifier
    ;
functionTypeParams
    : typeIdentifier (',' typeIdentifier)*
    ;
complexTypeIdentifier
    : IDENTIFIER ('.' IDENTIFIER)*
    ;

// Parameters
parameterList
    : parameter (',' parameter)*
    ;

parameter
    : typeIdentifier? identifier
    ;

// Arguments
argumentList
    : expression (',' expression)*
    ;

// Identifiers
identifier
    : STRING
    | IDENTIFIER
    ;

// Literals
literal
    : NUMBER
    | STRING
    | TRUE
    | FALSE
    | NULL
    ;

// ============================================================================
// LEXER RULES (uppercase)
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
RBRACE      : '}' ;
LBRACK      : '[' ;
RBRACK      : ']' ;
COMMA       : ',' ;
COLON       : ':' ;
SEMICOLON   : ';' ;

// Literals
NUMBER
    : [0-9]+ ('.' [0-9]+)?
    ;

STRING
    : '"' DoubleStringCharacter* '"'
    | '\'' SingleStringCharacter* '\''
    ;

fragment
DoubleStringCharacter
    : ~["\\\r\n]
    | EscapeSequence
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
