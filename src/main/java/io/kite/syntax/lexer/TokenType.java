package io.kite.syntax.lexer;

import org.apache.commons.lang3.ArrayUtils;

// var x = 40 + (foo * bar)
// [VarToken, IdentifierToken, EqualsToken, IntegerToken]
public enum TokenType {

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
    UnionType("|"),
    Logical_Not("!"),
    Schema("schema"),
    Return("return"),
    Init("init"),
    For("for"),
    In("in"),
    While("while"),
    If("if"),
    Else("else"),
    False("false"),
    True("true"),
    Fun("fun"),
    Type("type"),
//    Val("val"),
    Var("var"),
    Input("input"),
    Output("output"),
    This("this"),


    /******   IAC   *****/
    Resource("resource"),
    Component("component"),

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
    Range(".."),

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

}
