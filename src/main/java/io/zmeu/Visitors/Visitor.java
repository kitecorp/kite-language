package io.zmeu.Visitors;

import io.zmeu.Frontend.Lexical.Resolver;
import io.zmeu.Frontend.Parser.Expressions.*;
import io.zmeu.Frontend.Parser.Literals.*;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Frontend.Parser.Statements.*;
import io.zmeu.TypeChecker.Types.Type;
import io.zmeu.TypeChecker.TypeChecker;
import io.zmeu.Runtime.Interpreter;

public sealed interface Visitor<R>
        permits Resolver, TypeChecker, Interpreter, AstPrinter, LanguageAstPrinter, SyntaxPrinter {

    default R visit(Expression expr) {
        return switch (expr) {
            case BinaryExpression expression -> visit(expression);
            case AssignmentExpression expression -> visit(expression);
            case CallExpression expression -> visit((CallExpression<Expression>) expression);
            case ErrorExpression expression -> visit(expression);
            case GroupExpression expression -> visit(expression);
            case LogicalExpression expression -> visit(expression);
            case MemberExpression expression -> visit(expression);
            case ThisExpression expression -> visit(expression);
            case UnaryExpression expression -> visit(expression);
            case VarDeclaration expression -> visit(expression);
            case ValDeclaration expression -> visit(expression);
            case Identifier identifier -> visit(identifier);
            case Literal literal -> visit(literal);
            case BlockExpression expression -> visit(expression);
            case LambdaExpression expression -> visit(expression);
            case Type type -> visit(type);
            case ObjectExpression expression -> visit(expression);
        };
    }

    default R visit(Literal expression) {
        return switch (expression) {
            case NumberLiteral number -> visit(number);
            case StringLiteral string -> visit(string);
            case BooleanLiteral bool -> visit(bool);
            case NullLiteral nullliteral -> visit(nullliteral);
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        };
    }

    default R visit(Statement statement){
        return switch (statement){
            case ResourceExpression resourceExpression -> visit(resourceExpression);
            case Program program -> visit(program);
            case ExpressionStatement expressionStatement -> visit(expressionStatement);
            case ForStatement forStatement -> visit(forStatement);
            case FunctionDeclaration functionDeclaration -> visit(functionDeclaration);
            case IfStatement ifStatement -> visit(ifStatement);
            case InitStatement initStatement -> visit(initStatement);
            case ReturnStatement returnStatement -> visit(returnStatement);
            case SchemaDeclaration schemaDeclaration -> visit(schemaDeclaration);
            case VarStatement varStatement -> visit(varStatement);
            case ValStatement valStatement -> visit(valStatement);
            case WhileStatement whileStatement -> visit(whileStatement);
            default -> throw new IllegalStateException("Unexpected value: " + statement);
        };
    }


    R visit(NumberLiteral expression);

    R visit(BooleanLiteral expression);

    R visit(Identifier expression);

    R visit(NullLiteral expression);

    R visit(StringLiteral expression);

    R visit(LambdaExpression expression);

    R visit(BlockExpression expression);

    R visit(GroupExpression expression);

    R visit(BinaryExpression expression);

    R visit(CallExpression<Expression> expression);

    R visit(ErrorExpression expression);

    R visit(LogicalExpression expression);

    R visit(MemberExpression expression);

    R visit(ThisExpression expression);

    R visit(UnaryExpression expression);

    R visit(VarDeclaration expression);

    R visit(ValDeclaration expression);

    R visit(AssignmentExpression expression);

    R visit(float expression);

    R visit(double expression);

    R visit(int expression);

    R visit(boolean expression);

    R visit(String expression);

    R visit(Program program);


    R visit(Type type);

    /**
     * InitStatement
     * Syntactic sugar for a function
     */
    R visit(InitStatement statement);

    R visit(FunctionDeclaration statement);

    R visit(ExpressionStatement statement);

    R visit(VarStatement statement);

    R visit(ValStatement statement);

    R visit(IfStatement statement);

    R visit(WhileStatement statement);

    R visit(ForStatement statement);

    R visit(SchemaDeclaration statement);

    R visit(ReturnStatement statement);

    /**
     * An instance of a Schema is an Environment!
     * the 'parent' component of the instance environment is set to the class environment making class members accessible
     */
    R visit(ResourceExpression expression);
}
