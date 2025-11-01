package io.kite.Visitors;

import io.kite.Frontend.Lexical.ScopeResolver;
import io.kite.Frontend.Parse.Literals.*;
import io.kite.Frontend.Parser.Expressions.*;
import io.kite.Frontend.Parser.Program;
import io.kite.Frontend.Parser.Statements.*;
import io.kite.Runtime.Inputs.InputChainResolver;
import io.kite.Runtime.ResourcePath;
import io.kite.Runtime.exceptions.OperationNotImplementedException;
import io.kite.TypeChecker.Types.Type;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public sealed interface Visitor<R>
        permits ScopeResolver, InputChainResolver, StackVisitor, SyntaxPrinter {

    default R visit(@Nullable Expression expr) {
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
            case ArrayExpression arrayExpression -> visit(arrayExpression);
            case AnnotationDeclaration annotationDeclaration -> visit(annotationDeclaration);
            case null -> null;
        };
    }

    default R visit(Literal expression) {
        return switch (expression) {
            case NumberLiteral number -> visit(number);
            case StringLiteral string -> visit(string);
            case BooleanLiteral bool -> visit(bool);
            case NullLiteral nullliteral -> visit(nullliteral);
            case ObjectLiteral object -> visit(object);
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        };
    }

    default R visit(@Nullable Statement statement) {
        return switch (statement) {
            case ResourceStatement resourceStatement -> visit(resourceStatement);
            case Program program -> visit(program);
            case ExpressionStatement expressionStatement -> visit(expressionStatement);
            case ForStatement forStatement -> visit(forStatement);
            case FunctionDeclaration functionDeclaration -> visit(functionDeclaration);
            case IfStatement ifStatement -> visit(ifStatement);
            case InitStatement initStatement -> visit(initStatement);
            case ReturnStatement returnStatement -> visit(returnStatement);
            case SchemaDeclaration schemaDeclaration -> visit(schemaDeclaration);
            case ComponentStatement component -> visit(component);
            case InputDeclaration expression -> visit(expression);
            case OutputDeclaration expression -> visit(expression);
            case VarStatement varStatement -> visit(varStatement);
            case ValStatement valStatement -> visit(valStatement);
            case WhileStatement whileStatement -> visit(whileStatement);
            case UnionTypeStatement unionTypeStatement -> visit(unionTypeStatement);
            default -> throw new IllegalStateException("Unexpected value: " + statement);
        };
    }


    R visit(NumberLiteral expression);

    R visit(BooleanLiteral expression);

    R visit(Identifier expression);

    R visit(NullLiteral expression);

    R visit(ObjectLiteral expression);

    R visit(StringLiteral expression);

    R visit(LambdaExpression expression);

    R visit(BlockExpression expression);

    R visit(GroupExpression expression);

    R visit(BinaryExpression expression);

    R visit(UnionTypeStatement expression);

    R visit(CallExpression<Expression> expression);

    R visit(ErrorExpression expression);

    R visit(ComponentStatement expression);

    R visit(InputDeclaration expression);

    R visit(OutputDeclaration expression);

    R visit(LogicalExpression expression);

    R visit(MemberExpression expression);

    R visit(ThisExpression expression);

    R visit(UnaryExpression expression);

    R visit(VarDeclaration expression);

    R visit(ValDeclaration expression);

    R visit(ObjectExpression expression);

    R visit(ArrayExpression expression);

    R visit(AnnotationDeclaration expression);

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
    R visit(ResourceStatement expression);

    default ResourcePath resourceName(ResourceStatement resource) {
        var resourceName = switch (resource.getName()) {
            case SymbolIdentifier identifier -> identifier.string();
            case StringLiteral literal -> literal.getValue();
            case Identifier identifier -> identifier.string();
            case MemberExpression memberExpression -> visit(memberExpression).toString();
            case null, default ->
                    throw new OperationNotImplementedException("Resource name not implemented for: " + visit(resource));
        };
        var path = ResourcePath.parse(resource.getType().string() + "." + resourceName);
        return switch (resource.getIndex()) {
            case SymbolIdentifier id -> path.appendKey(id.string());
            case StringLiteral literal -> path.appendKey(literal.getValue());
            case Identifier id -> path.appendKey(id.string());
            case Map<?, ?> map -> path.appendKey(map.toString());
            case null -> path;
            default -> path.appendIndex(resource.getIndex());
        };
    }
}
