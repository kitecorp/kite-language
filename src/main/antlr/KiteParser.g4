parser grammar KiteParser;

options { tokenVocab = KiteLexer; }

// ============================================================================
// PARSER RULES
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
    : importStatement
    | declaration
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

// Import Statement
importStatement
    : IMPORT '*' FROM stringLiteral
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
    : SCHEMA identifier LBRACE statementTerminator* schemaPropertyList? statementTerminator* RBRACE
    ;

schemaPropertyList
    : schemaProperty (statementTerminator+ schemaProperty)* statementTerminator*
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
    | stringLiteral
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
    : IF '(' NL* expression NL* ')' NL* blockExpression elseStatement?
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
    : WHILE '(' NL* expression NL* ')' NL* blockExpression
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
    : '@' identifier ('(' NL* decoratorArgs? NL* ')')?
    ;

decoratorArgs
    : decoratorArg                      // Single positional: @provider("aws")
    | namedArg (NL* ',' NL* namedArg)* (NL* ',')? NL*  // Named args: @provider(first="aws", second="gcp")
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
    : ('-' | '++' | '--' | '!') unaryExpression  // Prefix: --x
    | postfixExpression
    ;

postfixExpression
    : leftHandSideExpression ('++' | '--')?      // Postfix: x++
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
    : LBRACE statementTerminator* statementList? statementTerminator* RBRACE
    ;

objectExpression
    : objectDeclaration
    ;

objectDeclaration
    : OBJECT LPAREN NL* (LBRACE NL* objectPropertyList? NL* RBRACE)? NL* RPAREN  // object() or object({ key: value })
    | LBRACE NL* objectPropertyList? NL* RBRACE                                   // { key: value }
    ;

objectPropertyList
    : objectProperty (NL* ',' NL* objectProperty)* (NL* ',')? NL*
    ;

objectProperty
    : objectKey objectInitializer?
    ;

objectKey
    : stringLiteral
    | IDENTIFIER
    | keyword
    ;

// Allow keywords as object property names (e.g., {type: "value"})
keyword
    : RESOURCE | COMPONENT | SCHEMA | INPUT | OUTPUT
    | IF | ELSE | WHILE | FOR | IN | RETURN
    | FUN | VAR | TYPE | INIT | THIS
    | OBJECT | ANY
    | TRUE | FALSE | NULL
    ;

objectInitializer
    : ':' expression
    ;

arrayExpression
    : '[' NL* FOR identifier (',' identifier)? IN (rangeExpression | arrayExpression | identifier) ':' compactBody NL* ']'  // Form 1: [for ...: body]
    | '[' NL* FOR identifier (',' identifier)? IN (rangeExpression | arrayExpression | identifier) ']' NL* forBody         // Form 2: [for ...] body
    | '[' NL* arrayItems? NL* ']'                                                                                            // Form 3: literal array
    ;

compactBody
    : IF '(' expression ')' expression (ELSE expression)?  // Inline if
    | IF expression expression (ELSE expression)?          // Inline if without parens
    | ifStatement                                          // Block if
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
    : arrayItem (NL* ',' NL* arrayItem)* (NL* ',')?  NL*  // Add NL* around commas, support trailing comma
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
    : stringLiteral
    | IDENTIFIER
    ;

// ============================================================================
// STRING LITERALS WITH INTERPOLATION
// ============================================================================

// String literal - can be interpolated or simple
stringLiteral
    : interpolatedString
    | SINGLE_STRING
    ;

// Interpolated string: "text ${expr} more text"
interpolatedString
    : DQUOTE stringPart* STRING_DQUOTE
    ;

// Parts of an interpolated string
stringPart
    : STRING_TEXT                           // Regular text
    | STRING_ESCAPE                         // Escaped character
    | STRING_DOLLAR                         // Lone $ not followed by {
    | INTERP_START expression INTERP_END    // ${expression}
    ;

// ============================================================================
// OTHER LITERALS
// ============================================================================

literal
    : NUMBER
    | stringLiteral
    | TRUE
    | FALSE
    | NULL
    ;
