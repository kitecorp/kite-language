package io.zmeu.Frontend.Lexer;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

// var x = 40 + (foo * bar)
// [VarToken, IdentifierToken, EqualsToken, IntegerToken]
public enum TokenType {
    /*****   Literal types   *****/
    Number("number"),
    Identifier("symbol"),
    Object("object"),
    String("string"),
    //    Type("type"),
    WhiteSpace("whitespace"),
    Comment("//"),
    NewLine(System.lineSeparator()),
    /**
     * ;
     */
    SemiColon(";"),
    Colon(":"),

    /******   Expressions   ******/
    Equal("="),
    Equal_Complex("+="),
    RelationalOperator("<>="),
    LessThanOperator("<"),
    LessThanOrEqualOperator("<="),
    GreaterThanOperator(">"),
    GreaterThanOrEqualOperator(">="),
    Equality_Operator("=="),
    /***********   Keywords   ******************/
    Logical_And("&&"),
    Logical_Or("||"),
    Logical_Not("!"),
    Schema("schema"),
    Return("return"),
    Init("init"),
    For("for"),
    While("while"),
    If("if"),
    Else("else"),
    False("false"),
    True("true"),
    Fun("fun"),
    Type("type"),
//    Val("val"),
    Var("var"),
    Param("param"),
    This("this"),


    /******   IAC   *****/
    Resource("resource"),
    Existing("existing"),
    Module("module"),

    /******   Visibility *****/
     /* all properties are private by default. This means that:
     1. the property/module will be accessible from other files
     2. the property/module won't be logged
     3. the property/module will appear in state file/deployment history
     */
    Public("public"),
    /* all properties are private by default. This means that:
     1. the property/module won't be accessible from other files
     2. the property/module won't be logged
     3. the property/module will appear in state file/deployment history
     */
    Private("private"),
    /* all properties are private by default. This means that:
     1. the property/module will be accessible from other files (access some secure password)
     2. the property/module won't be logged
     3. the property/module won't appear in state file/deployment history
     */
    Secure("secure"),
    /* all properties are private by default. This means that:
     1. the property/module will be accessible from other files
     2. the property/module will be logged
     3. the property/module will appear in state file/deployment history
     */

    /*****   Grouping   *****/

    /**
     * Braces: {}
     * Brackets: []
     * Parenthesis: ()
     */
    OpenParenthesis("("),
    CloseParenthesis(")"),

    /**
     * Braces: {}
     * Brackets: []
     * Parenthesis: ()
     */
    OpenBraces("{"),
    CloseBraces("}"),
    /**
     * Braces: {}
     * Brackets: []
     * Parenthesis: ()
     */
    OpenBrackets("["),
    CloseBrackets("]"),
    Comma(","),
    Dot("."),

    /*****   Operators   ******/
    OptionalOperator("?"),
    AT("@"),
    Plus("+"), Increment("++"), Decrement("--"), Minus("-"),
    Lambda("->"), Multiply("*"), Division("/"), Modulo("%"),
    Null("null"),
    EOF("EOF"),
    Unknown("Unknown");

    @Override
    public java.lang.String toString() {
        return field;
    }

    public java.lang.String getField() {
        return field;
    }

    private final String field;

    TokenType(String field) {
        this.field = field;
    }

    public static TokenType toSymbol(char token) {
        return switch (token) {
            case '(' -> OpenParenthesis;
            case ')' -> CloseParenthesis;
            case '{' -> OpenBraces;
            case '}' -> CloseBraces;
            case '[' -> OpenBrackets;
            case ']' -> CloseBrackets;
            case '?' -> OptionalOperator;
            case '@' -> AT;
            case '<' -> LessThanOperator;
            case '>' -> GreaterThanOperator;
            case '+' -> Plus;
            case '-' -> Minus;
            case '*' -> Multiply;
            case '/' -> Division;
            case '%' -> Modulo;
            default -> Unknown;
        };
    }


    public static boolean in(String operator, String... symbols) {
        return ArrayUtils.contains(symbols, operator);
    }

    public static boolean isAny(TokenType operator, TokenType... symbols) {
        return ArrayUtils.contains(symbols, operator);
    }

    public static TokenType toSymbol(String token) {
        if (token.length() > 1) {
            return switch (token) {
                case "||" -> Logical_Or;
                case "&&" -> Logical_And;
                default -> Logical_Not;
            };
        } else {
            return toSymbol(token.charAt(0));
        }
    }

    public static boolean isSymbol(char character) {
        return toSymbol(character) != Unknown;
    }

    public static TokenType toKeyword(String keyword) {
        return switch (keyword) {
            case "var" -> Var;
            case "param" -> Param;
            case "this" -> This;
            case "existing" -> Existing;
            case "resource" -> Resource;
            case "public" -> Public;
            case "private" -> Private;
            case "secure" -> Secure;
            case "module" -> Module;
            case "null" -> Null;
            case "and" -> Logical_And;
            case "or" -> Logical_Or;
            case "schema" -> Schema;
            case "return" -> Return;
            case "fun" -> Fun;
//            case "val" -> Val;
            case "if" -> If;
            case "else" -> Else;
            case "while" -> While;
            case "for" -> For;
            case "true" -> True;
            case "false" -> False;
            default -> Identifier;
        };
    }

    public static boolean isKeyword(String keyword) {
        return toKeyword(keyword) != Unknown;
    }

    public static boolean isLiteral(TokenType keyword) {
        return isAny(keyword, TokenType.String, TokenType.True, TokenType.False, TokenType.Null, TokenType.Number);
    }


    public static TokenType toNumber(char digit) {
        if (Character.isDigit(digit)) {
            return Number;
        }
        return Unknown;
    }

    public static boolean isSkippable(char character) {
        return Character.isWhitespace(character);
    }

    public static final List<TokenType> lineTerminator = List.of(NewLine, SemiColon);

}
