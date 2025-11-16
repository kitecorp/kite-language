package io.kite.Frontend.Lexical;

import io.kite.Frontend.Parse.Literals.*;
import io.kite.Frontend.Parser.Expressions.*;
import io.kite.Frontend.Parser.Program;
import io.kite.Frontend.Parser.Statements.*;
import io.kite.Frontend.Parser.ValidationException;
import io.kite.Runtime.exceptions.DeclarationExistsException;
import io.kite.TypeChecker.Types.Type;
import io.kite.Visitors.Visitor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public final class ScopeResolver implements Visitor<Void> {
    /**
     * Tracks how many scopes are we nested within source code. Based on this we know how to properly handle variable declarations/resolution
     * boolean = false => variable declared but not ready to be used
     * boolean = true => variable declared and ready to be used
     */
    private final Stack<Map<Identifier, Boolean>> scopes;
    /**
     * Tracks weather we're in a function or not. Based on this we show a syntax
     * error for example when using a return statement outside a function
     */
    private FunctionType currentFunction = FunctionType.NONE;


    public ScopeResolver() {
        scopes = new Stack<>();
    }

    void resolve(List<Statement> statements) {
        for (var statement : statements) {
            resolve(statement);
        }
    }

    public void resolve(Program program) {
        for (var statement : program.getBody()) {
            resolve(statement);
        }
    }

    private void resolve(@NotNull Statement stmt) {
        Visitor.super.visit(stmt);
    }

    private void resolve(@NotNull Expression expr) {
        Visitor.super.visit(expr);
    }

    @Override
    public Void visit(Expression expression) {
        resolve(expression);
        return null;
    }

    @Override
    public Void visit(NumberLiteral expression) {
        return null;
    }

    @Override
    public Void visit(BooleanLiteral expression) {
        return null;
    }

    @Override
    public Void visit(/* VariableExpression*/ Identifier identifier) {
        if (!scopes.isEmpty()) {
            if (scopes.peek().get(identifier.string()) == Boolean.FALSE) {
                throw new ValidationException("Can't read local variable in its own initializer: " + identifier.string());
            }
        }

        resolveLocal(identifier);
        return null;
    }

    private void resolveLocal(Identifier identifier) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(identifier)) {
                identifier.setHops(scopes.size() - 1 - i);
                break;
            }
        }

    }

    @Override
    public Void visit(BlockExpression expression) {
        beginScope();
        expression.getExpression().forEach(this::resolve);
        endScope();
        return null;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    @Override
    public Void visit(GroupExpression expression) {
        resolve(expression.getExpression());
        return null;
    }

    @Override
    public Void visit(BinaryExpression expression) {
        resolve(expression.getLeft());
        resolve(expression.getRight());
        return null;
    }

    @Override
    public Void visit(UnionTypeStatement expression) {
        declare(expression.getName());
        define(expression.getName());
        visit(expression.getName());

        for (Expression expressionExpression : expression.getExpressions()) {
            if (expressionExpression instanceof Identifier identifier) {
                declare(identifier);
                define(identifier);
                visit(expressionExpression);
            }
        }
        return null;
    }

    @Override
    public Void visit(CallExpression<Expression> expression) {
        resolve(expression.getCallee());
        for (Expression argument : expression.getArguments()) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visit(ErrorExpression expression) {
        return null;
    }

    @Override
    public Void visit(InputDeclaration expression) {
        visit(expression.getId());
        if (expression.hasType()) {
            visit(expression.getType());
        }
        if (expression.hasInit()) {
            visit(expression.getInit());
        }
        return null;
    }

    @Override
    public Void visit(OutputDeclaration expression) {
        visit(expression.getId());
        if (expression.hasType()) {
            visit(expression.getType());
        }
        if (expression.hasInit()) {
            visit(expression.getInit());
        }
        return null;
    }

    @Override
    public Void visit(LogicalExpression expression) {
        resolve(expression.getLeft());
        resolve(expression.getRight());
        return null;
    }

    @Override
    public Void visit(MemberExpression expression) {
        // Since properties are looked up dynamically, they don’t get resolved
        resolve(expression.getObject());
        return null;
    }

    @Override
    public Void visit(ThisExpression expression) {
        return null;
    }

    @Override
    public Void visit(UnaryExpression expression) {
        resolve(expression.getValue());
        return null;
    }

    @Override
    public Void visit(Program program) {
        return null;
    }

    @Override
    public Void visit(Type type) {
        return null;
    }

    @Override
    public Void visit(InitStatement statement) {
        return null;
    }

    @Override
    public Void visit(LambdaExpression expression) {
        resolveFunction(expression.getParams(), expression.getBody(), FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visit(FunctionDeclaration statement) {
        declare(statement.getName());
        define(statement.getName());

        resolveFunction(statement.getParams(), statement.getBody(), FunctionType.FUNCTION);
        return null;
    }

    private void resolveFunction(List<ParameterIdentifier> params, Statement body, FunctionType functionType) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = functionType;

        resolveFunction(params, body);

        currentFunction = enclosingFunction;
    }

    private void resolveFunction(List<ParameterIdentifier> params, Statement body) {
        beginScope(); // one for activation environment
        initParams(params);

        resolveNoBlock(body); //bypass function scope because we already are in the activation environment scope
        endScope();
    }

    private void initParams(List<ParameterIdentifier> function) {
        for (var param : function) {
            declare(param.getName());
            define(param.getName());
        }
    }

    /*
     this installs the params in the same scope as the function's body.
     without extracting and casting to BlockExpression it would create a separate
     scope for the body and a separate one for the params
     */
    private void resolveNoBlock(Statement statement) {
        if (statement instanceof ExpressionStatement body) {
            if (body.getStatement() instanceof BlockExpression block) {
                resolve(block.getExpression());
            } else {
                resolve(body.getStatement());
            }
        } else {
            throw new RuntimeException("oops: Statement is not an expression statement: " + statement.toString());
        }
    }

    @Override
    public Void visit(ExpressionStatement statement) {
        resolve(statement.getStatement());
        return null;
    }

    @Override
    public Void visit(VarStatement statement) {
        for (VarDeclaration declaration : statement.getDeclarations()) {
            visit(declaration);
        }
        return null;
    }

    @Override
    public Void visit(ValStatement statement) {
        for (ValDeclaration declaration : statement.getDeclarations()) {
            visit(declaration);
        }
        return null;
    }

    /**
     * Here, we see how resolution is different from interpretation. When we resolve an if statement, there is no control flow.
     * We resolve the condition and both branches. Where a dynamic execution steps only into the branch that is run,
     * a static analysis is conservative—it analyzes any branch that could be run.
     * Since either one could be reached at runtime, we resolve both.
     */
    @Override
    public Void visit(IfStatement statement) {
        resolve(statement.getTest());
        resolve(statement.getConsequent());
        if (statement.hasElse()) {
            resolve(statement.getAlternate());
        }
        return null;
    }

    /**
     * As in if statements, with a while statement, we resolve its condition and resolve the body exactly once.
     */
    @Override
    public Void visit(WhileStatement statement) {
        resolve(statement.getTest());
        resolve(statement.getBody());
        return null;
    }

    @Override
    public Void visit(ComponentStatement expression) {
        beginScope();
        if (expression.getName() != null) {
            switch (expression.getName()) {
                case SymbolIdentifier identifier -> {
                    declare(identifier);
                    define(identifier);
                }
                case Identifier id -> {
                    declare(id);
                    define(id);
                }
                case MemberExpression memberExpression -> {
                    if (memberExpression.getObject() instanceof SymbolIdentifier identifier) {
                        declare(identifier);
                        define(identifier);
                    }
//                    visit(memberExpression.getObject());
//                    visit(memberExpression.getProperty());
                }
                default -> {
                }
            }
            resolve(expression.getName());
        }
        for (Statement argument : expression.getArguments()) {
            visit(argument);
        }
        resolve(expression.getArguments());
        endScope();
        return null;
    }

    @Override
    public Void visit(ResourceStatement expression) {
        beginScope();
        if (expression.getName() != null) {
            switch (expression.getName()){
                case SymbolIdentifier identifier->{
                    declare(identifier);
                    define(identifier);
                }
                case Identifier id -> {
                    declare(id);
                    define(id);
                }
                case MemberExpression memberExpression -> {
                    if (memberExpression.getObject() instanceof SymbolIdentifier identifier){
                        declare(identifier);
                        define(identifier);
                    }
//                    visit(memberExpression.getObject());
//                    visit(memberExpression.getProperty());
                }
                default -> {
                }
            }
            resolve(expression.getName());
        }
        for (Statement argument : expression.getArguments()) {
            visit(argument);
        }
        resolve(expression.getArguments());
        endScope();
        return null;
    }

    @Override
    public Void visit(ForStatement statement) {
        if (statement.isBodyBlock()) {
            beginScope();
        }
        forInit(statement.getItem());
        forInit(statement.getIndex());
        if (statement.isBodyBlock()) {
            resolveNoBlock(statement.getBody()); // we are already inside the block opened above
        } else {
            resolve(statement.getBody());

        }
        if (statement.isBodyBlock()) {
            endScope();
        }
        return null;
    }

    private void forInit(Identifier statement) {
        if (statement != null) {
            declare(statement);
            resolve(statement);
            define(statement);
        }
    }

    @Override
    public Void visit(SchemaDeclaration statement) {
//        beginScope();
//        resolve(statement.getName());
//        resolveNoBlock(statement.getBody());
//        endScope();
        return null;
    }

    @Override
    public Void visit(ReturnStatement statement) {
        if (currentFunction == FunctionType.NONE) {
            throw new ValidationException("Can't return from top level code");
        }
        if (statement.hasArgument()) {
            resolve(statement.getArgument());
        }
        return null;
    }

    @Override
    public Void visit(VarDeclaration declaration) {
        declare(declaration.getId());
        if (declaration.hasInit()) {
            resolve(declaration.getInit());
        }
        define(declaration.getId());
        return null;
    }

    @Override
    public Void visit(ValDeclaration declaration) {
        declare(declaration.getId());
        if (declaration.hasInit()) {
            resolve(declaration.getInit());
        }
        define(declaration.getId());
        return null;
    }

    @Override
    public Void visit(ObjectExpression expression) {
        return null;
    }

    @Override
    public Void visit(ArrayExpression expression) {
        beginScope();
        if (expression.hasForStatement()) {
            resolve(expression.getForStatement());
        } else if (expression.hasItems()) {
            expression.getItems().forEach(this::resolve);
        }
        endScope();

        return null;
    }

    @Override
    public Void visit(AnnotationDeclaration expression) {
        return null;
    }

    /**
     * Declaration adds the variable to the innermost scope so that it shadows any outer one and so that we know the variable exists. We mark it as “not ready yet” by binding its name to false in the scope map.
     * The value associated with a key in the scope map represents whether or not we have finished resolving that variable’s initializer.
     */
    private void declare(Identifier name) {
        if (scopes.isEmpty()) return;

        Map<Identifier, Boolean> scope = scopes.peek();

        if (scope.containsKey(name)) {
            throw new DeclarationExistsException("Already a variable with this name in this scope: " + name.string());
        }

        scope.put(name, false);
    }

    /**
     * After declaring the variable, we resolve its initializer expression in that same scope where the new variable now exists but is unavailable.
     * Once the initializer expression is done, the variable is ready for prime time. We do that by defining it.
     */
    private void define(Identifier name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name, true);
    }

    @Override
    public Void visit(AssignmentExpression expression) {
        if (expression.getRight() instanceof Identifier right) {
//            declare(right);
//            define(right);
        }
        resolve(expression.getRight());

        if (expression.getLeft() instanceof Identifier left) {
//            declare(left);
//            define(left);
        }
        resolve(expression.getLeft());
        return null;
    }

    @Override
    public Void visit(float expression) {
        return null;
    }

    @Override
    public Void visit(double expression) {
        return null;
    }

    @Override
    public Void visit(int expression) {
        return null;
    }

    @Override
    public Void visit(boolean expression) {
        return null;
    }

    @Override
    public Void visit(String expression) {
        return null;
    }


    @Override
    public Void visit(NullLiteral expression) {
        return null;
    }

    @Override
    public Void visit(ObjectLiteral expression) {
        return null;
    }

    @Override
    public Void visit(StringLiteral expression) {
        return null;
    }

}
