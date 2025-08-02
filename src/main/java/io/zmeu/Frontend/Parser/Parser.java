package io.zmeu.Frontend.Parser;

import io.zmeu.BlockContext;
import io.zmeu.Frontend.Lexer.Token;
import io.zmeu.Frontend.Lexer.TokenType;
import io.zmeu.Frontend.Parse.Literals.*;
import io.zmeu.Frontend.Parser.Expressions.*;
import io.zmeu.Frontend.Parser.Statements.*;
import io.zmeu.Frontend.Parser.Statements.SchemaDeclaration.SchemaProperty;
import io.zmeu.Frontend.Parser.errors.ParseError;
import io.zmeu.ParserErrors;
import io.zmeu.Runtime.exceptions.InvalidInitException;
import io.zmeu.SchemaContext;
import io.zmeu.TypeChecker.Types.TypeParser;
import io.zmeu.TypeChecker.Types.ValueType;
import io.zmeu.Visitors.SyntaxPrinter;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.zmeu.Frontend.Lexer.TokenType.*;
import static io.zmeu.Frontend.Parse.Literals.Identifier.id;
import static io.zmeu.Frontend.Parse.Literals.ParameterIdentifier.param;
import static io.zmeu.Frontend.Parser.Expressions.AnnotationDeclaration.annotation;
import static io.zmeu.Frontend.Parser.Expressions.ArrayExpression.array;
import static io.zmeu.Frontend.Parser.Expressions.BinaryExpression.binary;
import static io.zmeu.Frontend.Parser.Statements.BlockExpression.block;
import static io.zmeu.Frontend.Parser.Statements.ExpressionStatement.expressionStatement;
import static io.zmeu.Frontend.Parser.Statements.SchemaDeclaration.SchemaProperty.schemaProperty;
import static io.zmeu.Frontend.Parser.Statements.SchemaDeclaration.schema;
import static io.zmeu.Frontend.Parser.Statements.VarStatement.varStatement;


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
     * this is ok because it's just a declaration but when in a resource, it must be initialised
     */
    private SchemaContext context;
    private BlockContext blockContext;

    public Parser(List<Token> tokens) {
        setTokens(tokens);
    }

    public Parser() {
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
            Statement statement = Declaration();
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

    private Statement Declaration() {
        try {
            return switch (lookAhead().type()) {
                case Fun -> FunctionDeclaration();
                case Type -> TypeDeclaration();
                case Schema -> SchemaDeclaration();
                case Existing, Resource -> ResourceDeclaration();
                case Module -> ModuleDeclaration();
                case Var -> VarDeclarations();
//                case Val -> ValDeclarations();
                default -> Statement();
            };
        } catch (RuntimeException error) {
            if (error instanceof ParseError parseError && parseError.getActual() != null) {
                String message = error.getMessage() + parseError.getActual().raw();
                ParserErrors.error(message);
                log.error(message);
            } else {
                ParserErrors.error(error.getMessage());
                log.error(error.getMessage());
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
        eat(In);
        Range<Integer> range = null;
        Identifier arrayName = null;
        if (IsLookAheadAfter(Number, Range)) {
            range = RangeDeclaration();
        } else {
            arrayName = SymbolIdentifier();
        }

//        var update = ForStatementIncrement();
        eatIf(CloseParenthesis, Colon, CloseBrackets);
        eatWhitespace();

        Statement body = null;
        if (IsLookAhead(Resource, Existing)) {
            body = ResourceDeclaration();
        } else {
            body = Statement();
        }
        return ForStatement.builder()
                .body(body)
                .range(range)
                .array(arrayName)
                .item(varName)
                .build();
    }

    @Nullable
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
    private Statement VarDeclarations() {
        eat(Var);
        var statement = VarStatementInit();
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
    private Statement VarStatementInit() {
        var declarations = VarDeclarationList();
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
     * ObjectStatement
     * : ObjectPropertyList
     */
    private Expression ObjectDeclaration() {
        eat(OpenBraces, "Object must start with { but it is: " + getIterator().getCurrent());
        Expression res;
        if (IsLookAhead(CloseBraces)) {
            res = ObjectExpression.objectExpression();
        } else {
            res = ObjectExpression.objectExpression(ObjectPropertyList());
        }
        eatWhitespace();
        if (IsLookAhead(CloseBraces)) { // ? { } => eat } & return the block
            eat(CloseBraces, "Object must end with } but it is: " + getIterator().getCurrent());
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
            eatWhitespace();
            declarations.add(ObjectLiteral());
        } while (IsLookAhead(Comma, NewLine) && eat(Comma, NewLine) != null && !IsLookAhead(CloseBraces));
        return declarations;
    }

    /**
     * ObjectExpression
     * : Identifier ':' ObjectInitialization?
     * ;
     */
    private ObjectLiteral ObjectLiteral() {
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
        if (IsLookAhead(Equal, Equal_Complex, Colon)) {
            eat(Equal, Equal_Complex, Colon);
        }
        return Expression();
    }

    /**
     * VarDeclarationList
     * : VarDeclaration
     * | VarDeclarationList , VarDeclaration
     * ;
     */
    private List<VarDeclaration> VarDeclarationList() {
        var declarations = new ArrayList<VarDeclaration>();
        do {
            declarations.add(VarDeclaration());
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
        if (context != SchemaContext.SCHEMA) {
            if (!IsLookAhead(Equal)) {
                throw new InvalidInitException(VAL_NOT_INITIALISED.formatted(id.string()));
            }
        }
        var init = ValInitializer();
        return ValDeclaration.val(id, type, init);
    }

    private TypeIdentifier TypeIdentifier() {
        if (context == SchemaContext.SCHEMA) {
            if (HasType()) { // type mandatory inside a schema. Init/default value is optional
                return typeParser.identifier();
            } else {
                throw new RuntimeException("Type declaration expected during schema declaration: var " + printer.visit(Identifier()));
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
        if (IsLookAhead(OpenBraces)) {
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
        if (IsLookAhead(OpenBraces)) {
            blockContext = BlockContext.OBJECT;
        }
        var res = Expression();
        blockContext = null;
        return res;
    }

    private Expression Expression() {
        return switch (lookAhead().type()) {
            case OpenBraces -> ObjectExpression();
            case OpenBrackets -> ArrayExpression();
            default -> AssignmentExpression();
        };
    }

    private @NotNull Expression ObjectExpression() {
        if (IsLookAheadAfter(Identifier, Colon) || context == SchemaContext.SCHEMA) {
            blockContext = BlockContext.OBJECT;
        }
        Expression expression;
        if (blockContext == BlockContext.OBJECT) {
            expression = ObjectDeclaration();
        } else {
            expression = BlockExpression();
        }
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
            return new ArrayTypeIdentifier(type);
        } else if (IsLookAhead(Number)) {
            eat(Number);
            var array = new ArrayTypeIdentifier(type);
            array.add(Literal());
            return array;
        } else if (IsLookAhead(For)) {
            throw new RuntimeException("For declaration in array not supported yet: " + getIterator().getCurrent());
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
            var optArray = OptArray();
            eat(CloseBrackets);
            return optArray;
        }
    }

    private @NotNull Expression OptArray() {
        return IsLookAhead(CloseBrackets) ? array() : ArrayItems();
    }

    /**
     * Parse array items. First item declares the type of the array and all following items must be of the same type
     */
    private ArrayExpression ArrayItems() {
        var array = new ArrayExpression();
        do {
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
        return switch (lookAhead().type()) {
            case Identifier -> SymbolIdentifier(); // Identifier() also checks for types
            case OpenBraces -> ObjectExpression();
            default -> {
                eat(Number, String, True, False, Object);
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
        var test = Expression();
        eatIf(CloseParenthesis);
        if (IsLookAhead(NewLine)) {
            // if(x) x=2
            eat(NewLine);
        }

        Statement ifBlock = Statement();
        Statement elseBlock = ElseStatement();
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

        Statement body = expressionStatement(BlockExpression());
        return FunctionDeclaration.fun(name, params, type, body);
    }

    /**
     * SchemaDeclaration
     * schema Name '{' SchemaProperty* '}'
     * ;
     */
    private Statement SchemaDeclaration() {
        eat(Schema);
        var schemaName = Identifier();

        context = SchemaContext.SCHEMA;
        eat(OpenBraces);
        var body = SchemaProperties();
        eat(CloseBraces);
        context = null;
        return schema(schemaName, body);
    }

    private List<SchemaProperty> SchemaProperties() {
        if (IsLookAhead(CloseBraces)) return Collections.emptyList();

        var params = new ArrayList<SchemaProperty>();
        while (IsLookAhead(lineTerminator) && eat(lineTerminator) != null && !IsLookAhead(CloseBraces)) {
            if (IsLookAhead(lineTerminator)) {
                continue;
            }
            params.add(SchemaProperty());
        }

        return params;
    }

    private SchemaProperty SchemaProperty() {
        AnnotationDeclaration annotation = null;
        if (IsLookAhead(AT)) {
            annotation = (AnnotationDeclaration) AnnotationDeclaration();
        }
        var statement = (VarStatement) VarDeclarations();
        return schemaProperty(statement.getDeclarations().get(0), annotation);
    }

    /**
     * Annotation
     * : '@' Identifier ( '(' ArgList ')' )?
     * ;
     */
    private Expression AnnotationDeclaration() {
        eat(AT);
        var name = Identifier();
        if (IsLookAhead(OpenParenthesis)) {
            eat(OpenParenthesis);
        }
        var statement = AnnotationArgs();
        if (IsLookAhead(CloseParenthesis)) {
            eat(CloseParenthesis);
        }
        return switch (statement) {
            case ArrayExpression identifier -> annotation(name, identifier);
            case ObjectExpression expression -> annotation(name, expression);
            case Identifier identifier -> annotation(name, identifier);
            case null -> annotation(name);
            default -> throw new IllegalStateException("Unexpected value: " + statement);
        };
    }

    private Expression AnnotationArgs() {
        switch (lookAhead().type()) {
            case OpenBrackets -> {
                return ArrayExpression();
            }
            case Identifier -> {
                return Identifier();
            }
            case OpenBraces -> {
                return ObjectDeclaration();
            }
            default -> {
                return null;
            }
        }
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
            } else { // enforce parameter type declaration
                throw ParserErrors.error("Type declaration expected for parameter: ", lookAhead(), lookAhead().type());
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
        return IsLookAhead(lineTerminator) ? io.zmeu.Frontend.Parse.Literals.TypeIdentifier.type(ValueType.Void) : Expression();
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
    private Statement ResourceDeclaration() {
        boolean existing = false;
        if (IsLookAhead(Existing)) {
            eat(Existing);
            existing = true;
        }
        eat(Resource);
        var type = typeParser.TypeIdentifier();
        Identifier name = null;
        if (IsLookAhead(TokenType.Identifier)) {
            name = Identifier();
        } else {
            throw ParserErrors.error("Missing identifier when declaring: resource " + type.string());
        }
        context = SchemaContext.RESOURCE;
        var body = BlockExpression("Expect '{' after resource name.", "Expect '}' after resource body.");
        context = null;
        return ResourceExpression.resource(existing, type, name, (BlockExpression) body);
    }

    /**
     * TypeDeclaration
     * type Name = TypeParams
     * ;
     */
    private Statement TypeDeclaration() {
        eat(Type);
        var name = Identifier();

        Expression body = TypeParams();
        return TypeExpression.type(name, body);
    }

    /**
     * : TypeParams
     * | '(' Literal | TypeIdentifier ')'[]?
     */
    private Expression TypeParams() {

        return null;
    }


    /**
     * ModuleDeclaration
     * : module TypeIdentifier name '{'
     * :    Inputs
     * : '}'
     * ;
     */
    private Statement ModuleDeclaration() {
        eat(Module);
        var moduleType = PluginIdentifier();
        var name = Identifier();
        var body = BlockExpression("Expect '{' after module name.", "Expect '}' after module body.");

        return ModuleExpression.of(moduleType, name, (BlockExpression) body);
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

    private PluginIdentifier PluginIdentifier() {
        switch (lookAhead().type()) {
            case String -> {
                var token = eat(String);
                return PluginIdentifier.fromString(token.value().toString());
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

    private Identifier ObjectKeyIdentifier() {
        return switch (lookAhead().type()) {
            case String -> {
                var id = eat(String);
                yield new SymbolIdentifier(id.value().toString());
            }
            default -> SymbolIdentifier();
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
        Token current = iterator.getCurrent();
        return switch (current.type()) {
            case True, False -> BooleanLiteral.bool(current.value());
            case Null -> NullLiteral.nullLiteral();
            case Number -> NumberLiteral.number(current.value());
            case String -> new StringLiteral(current.value());
            case Object -> new ObjectLiteral(id(current.value().toString()), Literal());
//            default -> new ErrorExpression(current.value());
            default -> NullLiteral.nullLiteral();
        };
    }

    boolean IsLookAheadAfter(TokenType after, TokenType... type) {
        return iterator.IsLookAheadAfter(after, type);
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
        return iterator.eat("Expected token: %s but it was %s".formatted(Arrays.toString(type).replaceAll("\\]?\\[?", ""), lookAhead().raw()), type);
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
