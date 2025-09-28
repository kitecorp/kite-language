package io.kite.Frontend.Parser;

import io.kite.ContextStack;
import io.kite.Frontend.Lexer.Token;
import io.kite.Frontend.Lexer.TokenType;
import io.kite.Frontend.Parse.Literals.*;
import io.kite.Frontend.Parser.Expressions.*;
import io.kite.Frontend.Parser.Statements.*;
import io.kite.Frontend.Parser.errors.ErrorList;
import io.kite.Frontend.Parser.errors.ParseError;
import io.kite.Frontend.annotations.Annotatable;
import io.kite.Runtime.exceptions.InvalidInitException;
import io.kite.TypeChecker.Types.TypeParser;
import io.kite.TypeChecker.Types.ValueType;
import io.kite.Visitors.SyntaxPrinter;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringEscapeUtils;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;

import static io.kite.Frontend.Lexer.TokenType.*;
import static io.kite.Frontend.Parse.Literals.ArrayTypeIdentifier.arrayType;
import static io.kite.Frontend.Parse.Literals.BooleanLiteral.bool;
import static io.kite.Frontend.Parse.Literals.NullLiteral.nullLiteral;
import static io.kite.Frontend.Parse.Literals.NumberLiteral.number;
import static io.kite.Frontend.Parse.Literals.ParameterIdentifier.param;
import static io.kite.Frontend.Parse.Literals.StringLiteral.string;
import static io.kite.Frontend.Parser.Expressions.AnnotationDeclaration.annotation;
import static io.kite.Frontend.Parser.Expressions.ArrayExpression.array;
import static io.kite.Frontend.Parser.Expressions.BinaryExpression.binary;
import static io.kite.Frontend.Parser.Statements.BlockExpression.block;
import static io.kite.Frontend.Parser.Statements.ExpressionStatement.expressionStatement;
import static io.kite.Frontend.Parser.Statements.SchemaDeclaration.schema;
import static io.kite.Frontend.Parser.Statements.SchemaProperty.schemaProperty;
import static io.kite.Frontend.Parser.Statements.VarStatement.varStatement;
import static java.text.MessageFormat.format;


/**
 * Name         Operators       Associates
 * Equality     == !=           Left
 * Comparison   < >= < <=       Left
 * Term         - +             Left
 * Factor       * /             Left
 * Unary        ! -             Right
 * Each rule here only matches expressions at its precedence level or higher.
 * For example, unary matches a unary expression like !negated or a primary expression like 1234
 * And term can match 1 + 2 but also 3 * 4 / 5. The final primary rule covers the highest-precedence
 * forms—literals and parenthesized expressions.
 * <p>
 * Expression -> Equality
 * Equality -> Comparison ( ("==" | "!=") Comparison)* ;
 * Comparison -> Term ( ( "!=" | "==" ) Term )* ;
 * Term -> Factor ( ( "-" | "+" ) Factor )* ;
 * Factor -> Unary ( ( "/" | "*" ) Unary )* ;
 * Unary -> ( "!" | "-" ) Unary
 * | Primary ;
 * Primary -> NUMBER
 * | STRING
 * | "true"
 * | "false"
 * | "null"
 * | "(" Expression ")"
 * <p>
 * ----------------------------------------------------------
 * Grammar notation         Code representation
 * -----------------------------------------------------------
 * Terminal                 Code to match and consume a token
 * Nonterminal              Call to that rule’s function
 * |                        if or switch statement
 * + or *                   while or for loop
 * ?                        if statement
 * ------------------------------------------------------------
 */
@Data
@Log4j2
public class Parser {
    public static final String VAL_NOT_INITIALISED = "val \"%s\" must be initialized";
    public TypeParser typeParser = new TypeParser(this);
    private ParserIterator iterator;
    private Program program = new Program();
    private SyntaxPrinter printer = new SyntaxPrinter();
    /**
     * Used to detect when a val/var is declared in a schema or in a resource.
     * When in a schema, a val can be left uninitialised:
     * val String x
     * this is ok because it's just a type but when in a resource, it must be initialised
     */
    private BlockContext blockContext;
    private Deque<ContextStack> contextStack = new ArrayDeque<>();

    public Parser(List<Token> tokens) {
        setTokens(tokens);
    }

    public Parser() {
    }

    private static void logParseError(ParseError parseError) {
        var message = MessageFormat.format("{0}{1}", parseError.getMessage(), parseError.getActual().raw());
        System.out.println(Ansi.ansi().fgRed().a(message).reset().toString());
    }

    private static Object setTarget(Set<AnnotationDeclaration> annotation, Annotatable res) {
        for (AnnotationDeclaration annotationDeclaration : annotation) {
            annotationDeclaration.setTarget(res);
        }
        return res;
    }

    private void setTokens(List<Token> tokens) {
        iterator = new ParserIterator(tokens);
    }

    public Program produceAST(List<Token> tokens) {
        setTokens(tokens);
        return Program();
    }

    private Program Program() {
        var statements = StatementList(EOF);
        program.setBody(statements);
        return program;
    }

    /**
     * StatementList
     * : Statement
     * | StatementList Statement
     * ;
     */
    private List<Statement> StatementList(TokenType endTokenType) {
        var statementList = new ArrayList<Statement>();
        for (; iterator.hasNext(); eat()) {
            if (IsLookAhead(endTokenType)) { // need to check for EOF before doing any work
                break;
            }
            if (IsLookAhead(NewLine)) {
                continue;
            }

            var annotations = AnnotationDeclaration();// extract the annotations
            annotations.forEach(annotation -> statementList.add(ExpressionStatement.expressionStatement(annotation)));

            Statement statement = Declaration(annotations);
            if (statement == null || statement instanceof EmptyStatement) {
                if (iterator.hasNext()) {
                    continue;
                } else {
                    break;
                }
            }
//            if (iterator.getCurrent().isLineTerminator()) { // if we eat too much - going beyond lineTerminator -> go back 1 token
//                iterator.prev();
//            }
            statementList.add(statement);
            if (IsLookAhead(endTokenType) || iterator.getCurrent().type() == EOF) {
                // after some work is done, before calling iterator.next(),
                // we must check for EOF again or else we risk going outside the iterators bounds
                break;
            }

        }

        return statementList;
    }

    private Statement Declaration(Set<AnnotationDeclaration> annotations) {
        try {
            return switch (lookAhead().type()) {
                case Fun -> FunctionDeclaration();
                case Type -> TypeDeclaration();
                case Schema -> {
                    var res = SchemaDeclaration(annotations);
                    setTarget(annotations, res);
                    yield res;
                }
                case Existing, Resource -> {
                    var res = ResourceDeclaration(annotations);
                    setTarget(annotations, res);
                    yield res;
                }
                case Component -> {
                    var res = ComponentDeclaration(annotations);
                    setTarget(annotations, res);
                    yield res;
                }
                case Input -> {
                    var res = InputDeclaration(annotations);
                    setTarget(annotations, res);
                    yield res;
                }
                case Output -> {
                    var res = OutputDeclaration(annotations);
                    setTarget(annotations, res);
                    yield res;
                }
                case Var -> VarDeclarations(annotations);
//                case Val -> ValDeclarations();
                default -> Statement();
            };
        } catch (RuntimeException error) {
            switch (error) {
                case ParseError parseError when parseError.getActual() != null -> logParseError(parseError);
                case ErrorList errorList -> {
                    List<ParseError> errors = errorList.getErrors();
                    errors.forEach(Parser::logParseError);
                }
                default -> System.out.println(Ansi.ansi().fgRed().a(error.getMessage()).reset().toString());
            }
            iterator.synchronize();
            return null;
        }
    }

    /**
     * {@snippet
             *: Statement
     * | EmptyStatement
     * | VarStatement
     * | IfStatement
     * | IterationStatement
     * | ForStatement
     * | FunctionDeclarationStatement
     * | SchemaDeclarationStatement
     * | ReturnStatement
     * | ExpressionStatement
     * ;
     *}
     */
    @Nullable
    private Statement Statement() {
        return switch (lookAhead().type()) {
            case NewLine -> new EmptyStatement();
//            case OpenBraces -> BlockStatement();
            case If -> IfStatement();
            case Init -> InitStatement();
            case Return -> ReturnStatement();
            case While, For -> IterationStatement();
            case EOF -> null;
            default -> ExpressionStatement();
        };
    }

    private Statement IterationStatement() {
        return switch (lookAhead().type()) {
            case While -> WhileStatement();
            case For -> ForStatement();
            default -> throw new SyntaxError();
        };
    }

    /**
     * WhileStatement
     * : while ( Expression ) {? StatementList }?
     * ;
     */
    private Statement WhileStatement() {
        eat(While);
        eat(OpenParenthesis);
        var test = Expression();
        eat(CloseParenthesis);
        if (IsLookAhead(NewLine)) {
            /* while(x)
             *   x=2
             */
            eat(NewLine);
        }

        var statement = Statement();
        return WhileStatement.of(test, statement);
    }

    /**
     * ForStatement
     * : '['? 'for' Identifier 'in' Items ':'? '{' BlockExpression '}'? ']'? ResourceDeclaration
     * : Items
     * | Range
     * |
     * ;
     */
    private Statement ForStatement() {
        eat(For);

        var varName = Identifier();
        Identifier index = null;
        if (IsLookAhead(Comma)) {
            eat(Comma);
            index = IsLookAhead(Identifier) ? Identifier() : null;
            if (index != null) { // switch names. Index is always the first name in the for type
                var tmp = index;
                index = varName;
                varName = tmp;
            }
        }
        eat(In);
        Range<Integer> range = null;
        Expression arrayName = null;
        if (IsLookAheadAfter(Number, Range)) {
            range = RangeDeclaration();
        } else if (IsLookAhead(OpenBrackets)) {
            eat(OpenBrackets);
            arrayName = OptArray();
        } else {
            arrayName = SymbolIdentifier();
        }

//        var update = ForStatementIncrement();
        eatIf(CloseParenthesis, Colon, CloseBrackets);
        eatWhitespace();

        Statement body = null;
        if (IsLookAhead(Resource, Existing)) {
            body = ResourceDeclaration(Set.of());
        } else {
            body = ForBody();
        }
        return ForStatement.builder()
                .body(body)
                .range(range)
                .array(arrayName)
                .index(index)
                .item(varName)
                .build();
    }

    private Statement ForBody() {
        return switch (lookAhead().type()) {
            case If -> IfStatement();
            case NewLine -> new EmptyStatement();
            case EOF -> null;
            default -> ExpressionStatement();
        };
    }

    private Range<Integer> RangeDeclaration() {
        eat(Number);
        if (Literal() instanceof NumberLiteral minLiteral) {
            eatAll(Range, Number);
            if (Literal() instanceof NumberLiteral maxLiteral) {
                var min = minLiteral.getValue().intValue();
                var max = maxLiteral.getValue().intValue();
                return org.apache.commons.lang3.Range.of(min, max);
            } else {
                throw new IllegalArgumentException("Expected a number literal in a range expression but got: " + getIterator().getCurrent());
            }
        } else {
            throw new IllegalArgumentException("Expected a number literal in a range expression but got: " + getIterator().getCurrent());
        }
    }

    @Nullable
    private Expression ForStatementIncrement() {
        return IsLookAhead(CloseParenthesis) ? null : Expression();
    }

    @Nullable
    private Expression ForStatementTest() {
        return IsLookAhead(SemiColon) ? null : Expression();
    }

    /**
     * VarStatement
     * : var VarDeclarations LineTerminator
     * ;
     */
    private Statement VarDeclarations(Set<AnnotationDeclaration> annotations) {
        eat(Var);
        var statement = VarStatementInit(annotations);
        return statement;
    }

    /**
     * ValStatement
     * : val ValDeclarations = InitStatement LineTerminator
     * ;
     */
    private Statement ValDeclarations() {
//        eat(Val);
        var statement = ValStatementInit();
        return statement;
    }

    /**
     * ValStatementInit
     * : val ValStatements ";"
     */
    private Statement ValStatementInit() {
        var declarations = ValDeclarationList();
        return ValStatement.valStatement(declarations);
    }

    /**
     * VarStatementInit
     * : var VarStatements ";"
     */
    private Statement VarStatementInit(Set<AnnotationDeclaration> annotations) {
        var declarations = VarDeclarationList(annotations);
        return varStatement(declarations);
    }

    /**
     * ExpressionStatement
     * : Expression \n
     * ;
     */
    private Statement ExpressionStatement() {
        return expressionStatement(Expression());
    }

    /**
     * BlockStatement
     * : { Statements? }
     * ;
     * Statements
     * : Statement* Expression
     */
    private Expression BlockExpression() {
        return BlockExpression(null, "Error");
    }

    private Expression BlockExpression(String errorOpen, String errorClose) {
        eat(OpenBraces, errorOpen);
        Expression res = IsLookAhead(CloseBraces) ? block(Collections.emptyList()) : block(StatementList(CloseBraces));
        if (IsLookAhead(CloseBraces)) { // ? { } => eat } & return the block
            eat(CloseBraces, errorClose);
        }
        return res;
    }

    /**
     * ObjectDeclaration
     * : object '(' ObjectPropertyList ')'
     * ; '{' ObjectPropertyList? '}'
     * ;
     * ObjectPropertyList
     * : ObjectProperty ( ',' ObjectProperty )*
     * ;
     * ObjectProperty
     * : Identifier ':' ObjectInitializer?
     * ;
     * ObjectInitializer
     * : ':' Expression
     * |
     */
    private Expression ObjectDeclaration() {
        if (IsLookAhead(Object)) { // case object({})
            eat(Object);
            if (!IsLookAhead(OpenParenthesis)) {
                return ObjectExpression.objectExpression();
            }
        }
        if (IsLookAhead(OpenParenthesis)) {
            eat(OpenParenthesis);
        }
        if (IsLookAhead(OpenBraces)) { // { might be missing if the user just declares object (any general object)
            eat(OpenBraces, "Object must start with { but it is: " + getIterator().getCurrent());
        }
        Expression res;
        if (IsLookAhead(CloseBraces, EOF, CloseParenthesis, UnionType, Comma, CloseBrackets)) {
            res = ObjectExpression.objectExpression();
        } else {
            res = ObjectExpression.objectExpression(ObjectPropertyList());
        }
        eatWhitespace();
        if (IsLookAhead(CloseBraces)) { // ? { } => eat } & return the block
            eat(CloseBraces, "Object must end with } but it is: " + getIterator().getCurrent());
        }
        if (IsLookAhead(CloseParenthesis)) {
            eat(CloseParenthesis);
        }

        return res;
    }

    /**
     * ObjectPropertyList
     * : ObjectExpression
     * | ObjectPropertyList , ObjectExpression
     * ;
     */
    private List<ObjectLiteral> ObjectPropertyList() {
        var declarations = new ArrayList<ObjectLiteral>();
        do {

            var e = ObjectLiteral();
            if (e == null) {
                break;
            }
            declarations.add(e);
        } while (IsLookAhead(Comma, NewLine) && eat(Comma, NewLine) != null && !IsLookAhead(CloseBraces, EOF));
        return declarations;
    }

    /**
     * ObjectExpression
     * : Identifier ':' ObjectInitialization?
     * ;
     */
    private ObjectLiteral ObjectLiteral() {
        if (IsLookAhead(NewLine, Comma)) {
//            eatWhitespace();
            if (IsLookAhead(EOF)) {
                return null;
            }
        }
        var id = ObjectKeyIdentifier();
        var init = IsLookAhead(lineTerminator, Comma, EOF) ? null : ObjectInitializer();
        return ObjectLiteral.object(id, init);
    }

    private void eatWhitespace() {
        while (IsLookAhead(lineTerminator)) {
            eat();
        }
    }

    /**
     * ObjectInitializer
     * : ':' ObjectExpression
     */
    private Expression ObjectInitializer() {
        if (IsLookAhead(Colon)) {
            eat(Colon);
        }
        return Expression();
    }

    /**
     * VarDeclarationList
     * : VarDeclaration
     * | VarDeclarationList , VarDeclaration
     * ;
     */
    private List<VarDeclaration> VarDeclarationList(Set<AnnotationDeclaration> annotations) {
        var declarations = new ArrayList<VarDeclaration>();
        do {
            var e = VarDeclaration();
            e.setAnnotations(annotations);
            setTarget(annotations, e);
            declarations.add(e);
        } while (IsLookAhead(Comma) && eat(Comma) != null);
        return declarations;
    }

    /**
     * ValDeclarationList
     * : ValDeclaration
     * | ValDeclarationList , ValDeclaration
     * ;
     */
    private List<ValDeclaration> ValDeclarationList() {
        var declarations = new ArrayList<ValDeclaration>();
        do {
            declarations.add(ValDeclaration());
        } while (IsLookAhead(Comma) && eat(Comma) != null);
        return declarations;
    }

    /**
     * VarDeclaration
     * : TypeDeclaration? Identifier VarInitialization?
     * ;
     */
    private VarDeclaration VarDeclaration() {
        var type = TypeIdentifier();
        var id = Identifier();
        var init = IsLookAhead(lineTerminator, Comma, CloseBraces, EOF) ? null : VarInitializer();
        return VarDeclaration.of(id, type, init);
    }

    /**
     * ValDeclaration
     * : TypeDeclaration? Identifier = ValInitialization?
     * ;
     */
    private ValDeclaration ValDeclaration() {
        var type = TypeIdentifier();
        var id = Identifier();
        if (contextStack.peek() != ContextStack.Schema) {
            if (!IsLookAhead(Equal)) {
                throw new InvalidInitException(VAL_NOT_INITIALISED.formatted(id.string()));
            }
        }
        var init = ValInitializer();
        return ValDeclaration.val(id, type, init);
    }

    private TypeIdentifier TypeIdentifier() {
        if (contextStack.peek() == ContextStack.Schema) {
            if (HasType()) { // type mandatory inside a schema. Init/default value is optional
                TypeIdentifier type = typeParser.identifier();
                if (IsLookAhead(OpenBrackets)) {
                    type = ArrayDeclaration(type);
                }
                return type;
            } else {
                throw new RuntimeException("Type type expected during schema type: var " + printer.visit(Identifier()));
            }
        } else {
            TypeIdentifier type = null;
            if (HasType()) { // type not mandatory outside a schema
                type = typeParser.identifier();
            }
            if (IsLookAhead(OpenBrackets)) {
                type = ArrayDeclaration(type);
            }
            return type;
        }
    }

    /**
     * VarInitializer
     * : SIMPLE_ASSIGN ObjectExpression
     */
    private Expression VarInitializer() {
        if (IsLookAhead(Equal, Equal_Complex)) {
            eat(Equal, Equal_Complex);
        }
        return Initialize();
    }

    private Expression Initialize() {
        if (IsLookAhead(OpenBraces, Object)) {
            blockContext = BlockContext.OBJECT;
        }
        var res = Expression();
        blockContext = null;
        return res;
    }

    /**
     * ValInitializer
     * : SIMPLE_ASSIGN Expression
     */
    private Expression ValInitializer() {
        if (IsLookAhead(Equal, Equal_Complex)) {
            eat(Equal, Equal_Complex);
        } else { // when no init
            return null;
        }
        return Initialize();
    }

    private Expression Expression() {
        return switch (lookAhead().type()) {
            case OpenBraces, Object -> ObjectExpression();
            case OpenBrackets -> ArrayExpression();
            default -> AssignmentExpression();
        };
    }

    private @NotNull Expression ObjectExpression() {
        if (IsLookAheadAfterUntil(Identifier, CloseBraces, Colon)
            || IsLookAhead(Object)
            || contextStack.peek() == ContextStack.Schema) {
            blockContext = BlockContext.OBJECT;
        }

        Expression expression;
        if (blockContext == BlockContext.OBJECT) {
            expression = ObjectDeclaration();
        } else {
            expression = BlockExpression();
        }
        blockContext = null;
        return expression;
    }

    /**
     * ArrayDeclaration
     * ; []
     * | '[' NumberLiteral ']'
     * | '[' ForStatement ']'
     */
    private ArrayTypeIdentifier ArrayDeclaration(TypeIdentifier type) {
        eat(OpenBrackets);
        var expression = OptArrayDeclaration(type);
        eat(CloseBrackets);
        return expression;
    }

    private ArrayTypeIdentifier OptArrayDeclaration(TypeIdentifier type) {
        if (IsLookAhead(CloseBrackets)) {
            return arrayType(type);
        } else if (IsLookAhead(Number)) {
            eat(Number);
            var array = arrayType(type);
            array.add(Literal());
            return array;
        } else if (IsLookAhead(For)) {
            throw new RuntimeException("For type in array not supported yet: " + getIterator().getCurrent());
        } else {
            throw new RuntimeException("Can't index an array using token: " + getIterator().getCurrent());
        }
    }

    /**
     * ; ArrayExpression
     * : '[' ArrayItems ']'
     */
    private Expression ArrayExpression() {
        eat(OpenBrackets);
        if (IsLookAhead(For)) {
            var statement = (ForStatement) ForStatement();
            var expression = new ArrayExpression();
            expression.setForStatement(statement);
            return expression;
        } else {
            return OptArray();
        }
    }

    private @NotNull Expression OptArray() {
        var res = IsLookAhead(CloseBrackets) ? array() : ArrayItems();
        eatWhitespace();
        eat(CloseBrackets);
        return res;
    }

    /**
     * Parse array items. First item declares the type of the array and all following items must be of the same type
     */
    private ArrayExpression ArrayItems() {
        var array = new ArrayExpression();
        do {
            eatWhitespace();
            array.add(ArrayItem());
        } while (!IsLookAhead(CloseBrackets) &&
                 !IsLookAhead(EOF) &&
                 IsLookAhead(Comma) &&
                 eat(Comma) != null);
        return array;
    }

    /**
     * We return an expression because it can be a Literal or an Identifier (variable name)
     */
    private Expression ArrayItem() {
        if (IsLookAheadAfterUntil(Dot, List.of(Comma, CloseBrackets), Identifier)) {
            return MemberExpression();
        }
        return switch (lookAhead().type()) {
            case Identifier -> SymbolIdentifier(); // Identifier() also checks for types
            case OpenBraces, Object -> {
                blockContext = BlockContext.OBJECT;
                yield ObjectExpression();
            }
            default -> {
                eat(Number, String, True, False, Object, Null);
                yield Literal();
            }
        };
    }

    /**
     * IfStatement
     * : if '('? Expression ')'? Statement? (else Statement)?
     * ;
     */
    private Statement IfStatement() {
        eat(If);
        eatIf(OpenParenthesis);
        contextStack.push(ContextStack.If);
        var test = Expression();
        eatIf(CloseParenthesis);
        if (IsLookAhead(NewLine)) {
            // if(x) x=2
            eat(NewLine);
        }

        Statement ifBlock = Statement();
        Statement elseBlock = ElseStatement();
        contextStack.pop();
        return IfStatement.If(test, ifBlock, elseBlock);
    }

    private Statement ElseStatement() {
        if (IsLookAhead(Else)) {
            eat(Else);
            Statement alternate = Statement();
            iterator.eatIf(CloseBraces);
            return alternate;
        }
        return null;
    }

    /**
     * FunctionDeclarationStatement
     * : fun Identifier ( OptParameterList ) BlockStatement?
     * ;
     */
    private Statement FunctionDeclaration() {
        eat(Fun, "Fun token expected: " + lookAhead());
        var name = Identifier();
        eat(OpenParenthesis, "Expected '(' but got: " + lookAhead());
        var params = OptParameterList();
        eat(CloseParenthesis, "Expected ')' but got: " + lookAhead());
        var type = typeParser.identifier();
        contextStack.push(ContextStack.FUNCTION);

        Statement body = expressionStatement(BlockExpression());
        Statement fun = FunctionDeclaration.fun(name, params, type, body);
        contextStack.pop();
        return fun;
    }

    /**
     * SchemaDeclaration
     * schema Name '{' SchemaProperty* '}'
     * ;
     */
    private SchemaDeclaration SchemaDeclaration(Set<AnnotationDeclaration> annotations) {
        eat(Schema);

        contextStack.push(ContextStack.Schema);

        var schemaName = Identifier();

        eat(OpenBraces);
        var body = SchemaProperties();
        eat(CloseBraces);

        contextStack.pop();

        return schema(schemaName, body, annotations);
    }

    private List<SchemaProperty> SchemaProperties() {
        if (IsLookAhead(CloseBraces)) return Collections.emptyList();

        var params = new ArrayList<SchemaProperty>();
        while (!IsLookAhead(CloseBraces)) {
            eatWhitespace();

            if (IsLookAhead(CloseBraces)) {
                break;
            }
            params.add(SchemaProperty());
        }

        return params;
    }

    private SchemaProperty SchemaProperty() {
        var annotations = AnnotationDeclaration();

        var type = TypeIdentifier();
        var id = Identifier();
        var init = IsLookAhead(lineTerminator, Comma, CloseBraces, EOF) ? null : PropertyInitializer();
        var res = schemaProperty(type, id, init, annotations);
        setTarget(annotations, res);
        return res;
    }

    /**
     * PropertyInitializer
     * : SIMPLE_ASSIGN ObjectExpression
     */
    private Expression PropertyInitializer() {
        if (IsLookAhead(Equal)) {
            eat(Equal);
        }
        return Initialize();
    }

    /**
     * Annotation
     * : '@' Identifier ( '(' ArgList ')' )?
     * ;
     */
    private Set<AnnotationDeclaration> AnnotationDeclaration() {
        if (!IsLookAhead(AT)) return Set.of(); //efficient empty set

        return AnnotationList();
    }

    private Set<AnnotationDeclaration> AnnotationList() {
        var set = new HashSet<AnnotationDeclaration>(1, 1.0f);
        while (IsLookAhead(AT) && !IsLookAhead(EOF, NewLine)) {
            var annotation = getAnnotationDeclarations();
            set.add(annotation);
            eatWhitespace();
        }
        return set;
    }

    private AnnotationDeclaration getAnnotationDeclarations() {
        eat(AT);
        var name = Identifier();
        if (IsLookAhead(OpenParenthesis)) {
            eat(OpenParenthesis);
            var statement = AnnotationArgs();
            if (IsLookAhead(CloseParenthesis)) {
                eat(CloseParenthesis);
            }
            return switch (statement) {
                case ArrayExpression identifier -> annotation(name, identifier);
                case ObjectExpression expression -> annotation(name, expression);
                case Identifier identifier -> annotation(name, identifier);
                case StringLiteral literal -> annotation(name, literal);
                case BooleanLiteral literal -> annotation(name, literal);
                case NumberLiteral literal -> annotation(name, literal);
                case MemberExpression expression -> annotation(name, expression);
                case null -> annotation(name);
                default -> throw new IllegalStateException("Unexpected value: " + statement);
            };
        }
        return annotation(name);
    }

    private Expression AnnotationArgs() {
        if (IsLookAheadAfterUntil(Dot, List.of(OpenBrackets, CloseParenthesis), Identifier)) {
            return MemberExpression();
        }
        return switch (lookAhead().type()) {
            case OpenBrackets -> ArrayExpression();
            case Identifier -> Identifier();
            case OpenBraces -> ObjectDeclaration();
            case True, False, Number, String -> {
                eat();
                yield Literal();
            }
            case Minus -> {
                eat(Minus);
                Object value1 = eat().value();
                yield switch (value1) {
                    case Integer value -> number(-value);
                    case Double value -> number(-value);
                    case Float value -> number(-value);
                    case Long value -> number(-value);
                    case Short value -> number(-value);
                    case Byte value -> number(-value);
                    case null, default -> throw ParserErrors.error("Can't negate a null value");
                };
            }
            default -> throw ParserErrors.error("Unexpected token: " + lookAhead());
        };
    }

    /**
     * InitStatement
     * : init  ( OptParameterList ) BlockStatement?
     * ;
     */
    private Statement InitStatement() {
        eat(Init);
        eat(OpenParenthesis);
        var params = OptParameterList();
        eat(CloseParenthesis);

        Statement body = expressionStatement(BlockExpression());
        return InitStatement.of(params, body);
    }

    private List<ParameterIdentifier> OptParameterList() {
        return IsLookAhead(CloseParenthesis) ? Collections.emptyList() : ParameterList();
    }

    /**
     * ParameterList
     * : Identifier
     * | ParameterList, Identifier
     * ;
     */
    private List<ParameterIdentifier> ParameterList() {
        var params = new ArrayList<ParameterIdentifier>();
        do {
            params.add(FunParameter());
        } while (IsLookAhead(Comma) && eat(Comma) != null);

        return params;
    }

    private ParameterIdentifier FunParameter() {
        if (IsLookAhead(Identifier, OpenParenthesis)) { // OpenParenthesis because fun onClick(callback (Number)->Number) callback's type is a function
            TypeIdentifier type;
            if (IsLookAheadAfter(Identifier, Identifier)) { // param has type, parse it. If it doesn't the TypeChecker will throw an exception
                type = typeParser.identifier();
            } else { // enforce parameter type type
                throw ParserErrors.error("Type type expected for parameter: ", lookAhead(), lookAhead().type());
            }
            var symbol = SymbolIdentifier();
            return param(symbol, type);
        } else {
            TypeIdentifier type = null;
            if (HasType()) {
                type = typeParser.identifier();
            }
            var symbol = SymbolIdentifier();
            return param(symbol, type);
        }
    }

    private Statement ReturnStatement() {
        eat(Return);
        var arg = OptExpression();
        return ReturnStatement.funReturn(arg);
    }

    private Expression OptExpression() {
        return IsLookAhead(lineTerminator) ? TypeIdentifier.type(ValueType.Void) : Expression();
    }

    /**
     * LambdaExpression
     * : ( OptParameterList ) -> LambdaBody
     * | (( OptParameterList ) -> LambdaBody)()()
     * ;
     */
    private Expression LambdaExpression() {
        eat(OpenParenthesis);
        if (IsLookAhead(OpenParenthesis)) {
            var expression = LambdaExpression();
            eat(CloseParenthesis); // eat CloseParenthesis after lambda body
            return CallExpression.call(expression, Arguments());
        }

        var params = OptParameterList();
        eat(CloseParenthesis);
        var returnType = typeParser.identifier(); // eat returnType
        eat(Lambda, "Expected -> but got: " + lookAhead().value());

        return LambdaExpression.lambda(params, LambdaBody(), returnType);
    }

    private Statement LambdaBody() {
        return IsLookAhead(OpenBraces) ? Statement() : ExpressionStatement();
    }

    /**
     * A single token lookahead recursive descent parser can’t see far enough to tell that it’s parsing an assignment
     * until after it has gone through the left-hand side and stumbled onto the =.
     * You might wonder why it even needs to. After all, we don’t know we’re parsing a + expression until
     * after we’ve finished parsing the left operand.
     * <p>
     * The difference is that the left-hand side of an assignment isn’t an expression that evaluates to a value.
     * It’s a sort of pseudo-expression that evaluates to a “thing” you can assign to. Consider:
     * {@snippet :
     * var a = "before";
     * a = "value";
     *}
     * On the second line, we don’t evaluate a (which would return the string “before”).
     * We figure out what variable a refers to so we know where to store the right-hand side expression’s value.
     * All of the expressions that we’ve seen so far that produce values are r-values.
     * An l-value “evaluates” to a storage location that you can assign into.
     * We want the syntax tree to reflect that an l-value isn’t evaluated like a normal expression.
     * That’s why the Expr.Assign node has a Token for the left-hand side, not an Expr.
     * The problem is that the parser doesn’t know it’s parsing an l-value until it hits the =.
     * In a complex l-value, that may occur many tokens later.
     * {@snippet :
     *  makeList().head.next = node;
     *}
     */
    private Expression AssignmentExpression() {
        Expression left = OrExpression();
        if (IsLookAhead(Equal, Equal_Complex)) {
            var operator = AssignmentOperator().value();
            if (IsLookAhead(OpenBraces)) {
                blockContext = BlockContext.OBJECT;
            }
            Expression rhs = Expression();

            left = AssignmentExpression.assign(isValidAssignmentTarget(left, operator), rhs, operator);
        }
        return left;
    }

    // x || y
    private Expression OrExpression() {
        var expression = AndExpression();
        while (!IsLookAhead(EOF) && IsLookAhead(Logical_Or)) {
            var operator = eat();
            Expression right = AndExpression();
            expression = LogicalExpression.of(operator.value().toString(), expression, right);
        }
        return expression;
    }

    // x && y
    private Expression AndExpression() {
        var expression = EqualityExpression();
        while (!IsLookAhead(EOF) && IsLookAhead(Logical_And)) {
            var operator = eat();
            Expression right = EqualityExpression();
            expression = LogicalExpression.of(operator.value(), expression, right);
        }
        return expression;
    }

    /**
     * x == y
     * x != y
     */
    private Expression EqualityExpression() {
        var expression = RelationalExpression();
        while (!IsLookAhead(EOF) && IsLookAhead(Equality_Operator)) {
            var operator = eat();
            Expression right = EqualityExpression();
            expression = binary(expression, right, operator.value().toString());
        }
        return expression;
    }

    /**
     * x > y
     * x >= y
     * x < y
     * x <= y
     */
    private Expression RelationalExpression() {
        var expression = AdditiveExpression();
        while (!IsLookAhead(EOF) && IsLookAhead(RelationalOperator)) {
            var operator = eat();
            Expression right = RelationalExpression();
            expression = binary(expression, right, operator.value().toString());
        }
        return expression;
    }

    /**
     * AssignmentOperator: +, -=, +=, /=, *=
     */
    private Token AssignmentOperator() {
        Token token = lookAhead();
        if (token.isAssignment()) {
            return eat(token.type());
        }
        throw Error(token, "Unrecognized token");
    }

    private Expression isValidAssignmentTarget(Expression target, Object operator) {
        if (target instanceof Identifier || target instanceof MemberExpression) {
            return target;
        }
        Object value = iterator.getCurrent().value();
        if (target instanceof Literal n) {
            throw Error("Invalid left-hand side in assignment expression: %s %s %s".formatted(n.getVal(), operator, value));
        } else {
            throw Error("Invalid left-hand side in assignment expression: %s %s %s".formatted(printer.visit(target), operator, value));
        }
    }

    private RuntimeException Error(String message) {
        return Error(iterator.lookAhead(), message);
    }

    private RuntimeException Error(Token token, String message) {
        return ParserErrors.error(message, token);
    }


    /**
     * AdditiveExpression
     * : MultiplicativeExpression
     * | AdditiveExpression ADDITIVE_OPERATOR MultiplicativeExpression -> MultiplicativeExpression ADDITIVE_OPERATOR MultiplicativeExpression
     * ;
     */
    @Nullable
    private Expression AdditiveExpression() {
        var left = MultiplicativeExpression();

        // (10+5)-5
        while (match("+", "-")) {
            var operator = eat();
            Expression right = this.MultiplicativeExpression();
            left = binary(left, right, operator.value().toString());
        }

        return left;
    }

    private Expression MultiplicativeExpression() {
        var left = UnaryExpression();

        // (10*5)-5
        while (match("*", "/", "%")) {
            var operator = eat();
            Expression right = UnaryExpression();
            left = new BinaryExpression(left, right, operator.value().toString());
        }

        return left;
    }

    private Expression UnaryExpression() {
        var operator = switch (lookAhead().type()) {
            case Minus -> eat(Minus);
            case Increment -> eat(Increment);
            case Decrement -> eat(Decrement);
            case Logical_Not -> eat(Logical_Not);
            default -> null;
        };
        if (operator != null) {
            return UnaryExpression.of(operator.value(), UnaryExpression());
        }

        return LeftHandSideExpression();
    }

    @Nullable
    private Expression PrimaryExpression() {
        if (lookAhead() == null) {
            return null;
        }
        return switch (lookAhead().type()) {
            case OpenParenthesis, OpenBrackets -> ParenthesizedExpression();
            case Equal -> {
                eat();
                yield AssignmentExpression();
            }
            case Equality_Operator -> Literal();
            case Number, Object, String, True, False, Null -> /* literals */{
                eat();
                yield Literal();
            }
            case Identifier -> Identifier();
            case This -> ThisExpression();
            case EOF -> null;
            default -> LeftHandSideExpression();
        };
    }

    /**
     * ThisExpression
     * : this
     * ;
     */
    private Expression ThisExpression() {
        eat(This);
        return ThisExpression.of();
    }

    /**
     * ResourceDeclaration
     * : resource TypeIdentifier name '{'
     * :    VarDeclaration
     * : '}'
     * ;
     */
    private ResourceExpression ResourceDeclaration(Set<AnnotationDeclaration> annotations) {
        if (inContext(ContextStack.FUNCTION)) {
            throw ParserErrors.error("Resource type not allowed in function body: ", lookAhead(), lookAhead().type());
        }
        boolean existing = false;
        if (IsLookAhead(Existing)) {
            eat(Existing);
            existing = true;
        }
        eat(Resource);
        var type = typeParser.TypeIdentifier();
        Expression name = resourceSymbol(type);
        contextStack.push(ContextStack.Resource);
        var body = BlockExpression("Expect '{' after resource name.", "Expect '}' after resource body.");
        contextStack.pop();
        return ResourceExpression.resource(annotations, existing, type, name, (BlockExpression) body);
    }

    private boolean inContext(ContextStack stack) {
        return contextStack.contains(stack);
    }

    private Expression resourceSymbol(TypeIdentifier type) {
        if (IsLookAheadAfterUntil(Dot, OpenBraces, Identifier)) {
            return MemberExpression();
        } else if (IsLookAhead(TokenType.Identifier)) {
            return Identifier();
        } else if (IsLookAhead(String)) {
            var id = eat(String);
            return new SymbolIdentifier(id.value());
        }
        throw ParserErrors.error("Missing identifier when declaring: resource " + type.string());
    }

    /**
     * TypeDeclaration
     * type Name = TypeParams
     * ;
     */
    private Statement TypeDeclaration() {
        eat(Type);
        var name = Identifier();
        eat(Equal);

        var body = TypeParams();
        return UnionTypeStatement.union(name, body);
    }

    /**
     * : TypeParams
     * | (Literal '|')*
     * | (TypeIdentifier '|')*
     */
    private HashSet<Expression> TypeParams() {
        var params = new HashSet<Expression>();
        while (!IsLookAhead(lineTerminator) && !IsLookAhead(EOF)) {
            var param = ParseUnionType();
            if (params.contains(param)) {
                throw ParserErrors.error("Duplicate type parameter: " + printer.visit(param));
            }
            params.add(param);
            if (IsLookAhead(UnionType)) {
                eat(UnionType);
            }
        }
        return params;
    }

    private Expression ParseUnionType() {
        var param = switch (lookAhead().type()) {
            case String, Number, True, False, Null -> {
                eat(lookAhead().type());
                yield Literal();
            }
            case OpenBraces, Object -> ObjectDeclaration();
            case OpenBrackets -> ArrayExpression();
            case Identifier -> Identifier();
            default -> throw new IllegalStateException("Unexpected value: " + lookAhead().type());
        };
        return param;
    }


    /**
     * ComponentDeclaration
     * : component TypeIdentifier name '{'
     * :    Inputs
     * : '}'
     * ;
     */
    private ComponentStatement ComponentDeclaration(Set<AnnotationDeclaration> annotations) {
        eat(Component);
        var componentType = ComponentType();
        Identifier name = null;
        if (IsLookAhead(Identifier)) { // present when component is initialised. Absent when component is declared
            name = Identifier();
        }
        var body = (BlockExpression) BlockExpression("Expect '{' after component name.", "Expect '}' after component body.");

        validateInputsOutputs(name, body);

        return ComponentStatement.component(componentType, name, body, annotations);
    }

    private void validateInputsOutputs(Identifier componentName, BlockExpression body) {
        var set = new HashSet<String>();
        for (var statement : body.getExpression()) {
            switch (statement) {
                case InputDeclaration inputDeclaration -> {
                    if (componentName != null) {
                        throw ParserErrors.error("Component type should not have inputs");
                    }
                    if (!set.add(inputDeclaration.name())) {
                        throw ParserErrors.error(format("Duplicate input names in component `{0}` : {1}", printer.visit(componentName), printer.visit(inputDeclaration)));
                    }
                }
                case OutputDeclaration outputDeclaration -> {
                    if (componentName != null) {
                        throw ParserErrors.error("Component type should not have outputs");
                    }
                    if (!set.add(outputDeclaration.name())) {
                        throw ParserErrors.error(format("Duplicate outputs names in component `{0}` : {1}", printer.visit(componentName), printer.visit(outputDeclaration)));
                    }
                }
                case null, default -> {
                }
            }
        }
    }

    private InputDeclaration InputDeclaration(Set<AnnotationDeclaration> annotations) {
        eat(Input);
        var type = TypeIdentifier();
        var name = Identifier();
        Expression body = null;
        if (IsLookAhead(Equal)) {
            eat(Equal);
            body = Initialize();
        }
        return InputDeclaration.input(name, type, body, annotations);
    }

    private OutputDeclaration OutputDeclaration(Set<AnnotationDeclaration> annotations) {
        eat(Output);
        var type = TypeIdentifier();
        var name = Identifier();
        Expression body;
        if (IsLookAhead(Equal)) {
            eat(Equal);
            if (IsLookAhead(NewLine, WhiteSpace, EOF)) {
                throw ParserErrors.error("Missing value for `output %s %s = %s` ".formatted(type.string(), name.string(), StringEscapeUtils.escapeJava(lookAhead().value().toString())));
            }
            body = Initialize();
        } else {
            throw ParserErrors.error("Missing '=' after: output %s %s".formatted(type.string(), name.string()));
        }
        return OutputDeclaration.output(name, type, body, annotations);
    }

    private Expression LeftHandSideExpression() {
        return CallMemberExpression();
    }

    // bird.fly()
    private Expression CallMemberExpression() {
        var primaryIdentifier = MemberExpression(); // .fly
        while (true) {
            if (IsLookAhead(OpenParenthesis)) { // fly(
                primaryIdentifier = CallExpression.call(primaryIdentifier, Arguments());
            } else {
                break;
            }
        }
        return primaryIdentifier;
    }

    private List<Expression> Arguments() {
        eat(OpenParenthesis, "Expect '(' before arguments.");
        var list = ArgumentList();
        eat(CloseParenthesis, "Expect ')' after arguments.");
        return list;
    }

    private List<Expression> ArgumentList() {
        if (IsLookAhead(CloseParenthesis)) return Collections.emptyList();

        var arguments = new ArrayList<Expression>();
        do {
            if (arguments.size() >= 128) {
                throw Error(lookAhead(), "Can't have more than 128 arguments");
            }
            arguments.add(Expression());
        } while (match(Comma) && eat(Comma, "Expect ',' after argument: " + iterator.getCurrent().raw()) != null);

        return arguments;
    }

    /**
     * a.Expression
     * a[ Expression ]
     */
    private Expression MemberExpression() {
        var object = PrimaryExpression();
        for (var next = lookAhead(); IsLookAhead(Dot, OpenBrackets); next = lookAhead()) {
            object = switch (next.type()) {
                case Dot -> {
                    var property = MemberProperty();
                    yield MemberExpression.member(false, object, property);
                }
                case OpenBrackets -> {
                    var property = MemberPropertyIndex();
                    yield MemberExpression.member(true, object, property);
                }
                default -> throw new IllegalStateException("Unexpected value: " + next.type());
            };
        }
        return object;
    }

    private Expression MemberPropertyIndex() {
        eat(OpenBrackets);
        var property = Expression();
        eat(CloseBrackets);
        return property;
    }

    private Expression MemberProperty() {
        eat(Dot);
        return Identifier();
    }

    private PluginIdentifier ComponentType() {
        switch (lookAhead().type()) {
            case String -> {
                throw ParserErrors.error("Component type can't be a string ", lookAhead(), lookAhead().type());
//                var token = eat(String);
//                return PluginIdentifier.fromString(token.value().toString());
            }
            case Identifier -> {
                TypeIdentifier type = typeParser.TypeIdentifier();
                return PluginIdentifier.from(type);
            }
            case null, default -> throw new RuntimeException("Unexpected token type: " + lookAhead().type());
        }
    }

    private Identifier Identifier() {
        return switch (lookAhead().type()) {
            case String -> typeParser.TypeIdentifier();
            default -> SymbolIdentifier();
        };
    }

    /**
     * Key will always be a string as it cannot be a symbol like a variable so if it is a symbol we treat it like a string
     */
    private Expression ObjectKeyIdentifier() {
        return switch (lookAhead().type()) {
            case String -> {
                var id = eat(String);
                yield new StringLiteral(id.value().toString());
            }
            case Identifier -> {
                var id = eat(Identifier);
                yield new StringLiteral(id.value().toString());
            }
            case NewLine, Comma -> {
                eat();
                yield ObjectKeyIdentifier();
            }
            default -> throw new RuntimeException("Unexpected token type: " + lookAhead().type());
        };
    }

    private @NotNull SymbolIdentifier SymbolIdentifier() {
        var id = eat(TokenType.Identifier);
        return new SymbolIdentifier(id.value());
    }

    private Expression ParenthesizedExpression() {
        if (IsLookAheadAfter(CloseParenthesis, Lambda) || IsLookAheadAfter(Identifier, Lambda)) {
            // lookahead after () -> or () type ->
            return LambdaExpression();
        }
        eat(OpenParenthesis);
        var res = Expression();
        if (IsLookAhead(CloseParenthesis)) {
            eat(CloseParenthesis, "Unexpected token found inside parenthesized expression. Expected closed parenthesis.");
        } else if (IsLookAhead(CloseBraces)) {
            eat(CloseBraces, "Unexpected token found inside parenthesized expression. Expected closed parenthesis.");
        }
        return res;
    }

    private Literal Literal() {
        var current = iterator.getCurrent();
        var value = current.value();
        return switch (current.type()) {
            case True, False -> bool(value);
            case Null -> nullLiteral();
            case Number -> number(value);
            case String -> string(value);
//            default -> new ErrorExpression(current.value());
            default -> nullLiteral();
        };
    }

    boolean IsLookAheadAfter(TokenType after, TokenType... type) {
        return iterator.IsLookAheadAfter(after, type);
    }

    /**
     * Checks whether a given sequence of token types occurs ahead in the token stream,
     * bounded by a specified end token.
     * <p>
     * Starting from the current parser position (just after the last returned token),
     * this method scans forward until it either finds the {@code endToken} or runs out of tokens.
     * While scanning, if it encounters a token of type {@code after}, it looks immediately
     * at the next token. If that next token matches any of the types provided in {@code type},
     * the method returns {@code true}.
     * </p>
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * // Returns true if, before reaching a RIGHT_BRACE, we see a COLON followed by IDENTIFIER
     * IsLookAheadAfter(TokenType.COLON, TokenType.RIGHT_BRACE, TokenType.IDENTIFIER);
     * }</pre>
     *
     * @param after    the token type that triggers the lookahead check
     * @param endToken the token type that bounds the lookahead search; scanning stops when this is found
     * @param type     one or more token types to check immediately after {@code after}
     * @return {@code true} if a token of type {@code after} is followed by any of the {@code type}
     * before encountering {@code endToken}; otherwise {@code false}
     */
    boolean IsLookAheadAfterUntil(TokenType after, TokenType endToken, TokenType... type) {
        return iterator.IsLookAheadAfter(after, List.of(endToken), type);
    }

    boolean IsLookAheadAfterUntil(TokenType after, List<TokenType> endToken, TokenType... type) {
        return iterator.IsLookAheadAfter(after, endToken, type);
    }

    boolean HasType() {
        return iterator.hasType();
    }

    public Token lookAhead() {
        return iterator.lookAhead();
    }

    public Token eat() {
        return iterator.eat();
    }

    public Token eat(TokenType... type) {
        return iterator.eat("Expected token %s but it was '%s'".formatted(Arrays.toString(type).replaceAll("\\]?\\[?", ""), lookAhead().raw()), type);
    }

    public Token eatIf(TokenType... type) {
        if (IsLookAhead(type)) {
            return iterator.eat("Expected token: %s but it was %s".formatted(Arrays.toString(type).replaceAll("\\]?\\[?", ""), lookAhead().raw()), type);
        }
        return null;
    }

    public Token eatAll(TokenType... type) {
        Token token = null;
        for (var tokenType : type) {
            if (IsLookAhead(tokenType)) {
                token = eat(tokenType);
            }
        }
        return token;
    }

    public Token eat(List<TokenType> list) {
        Token token = null;
        for (var tokenType : list) {
            if (IsLookAhead(tokenType)) {
                token = eat(tokenType);
            }
        }
        return token;
    }

    public Token eat(TokenType type, String error) {
        return iterator.eat(error, type);
    }

    public boolean IsLookAhead(List<TokenType> list, TokenType... types) {
        return IsLookAhead(list) || IsLookAhead(types);
    }

    public boolean IsLookAhead(TokenType... type) {
        return iterator.IsLookAhead(type);
    }

    public boolean IsLookAhead(List<TokenType> type) {
        for (TokenType p : type) {
            if (iterator.IsLookAhead(p)) {
                return true;
            }
        }
        return false;
    }

    public boolean match(String... strings) {
        return iterator.hasNext() && lookAhead().is(strings);
    }

    public boolean match(TokenType... strings) {
        return iterator.hasNext() && IsLookAhead(strings);
    }

}
