package io.kite.Frontend.Parser.Statements;

import io.kite.Frontend.Parser.Expressions.ModuleExpression;
import io.kite.Frontend.Parser.Expressions.ResourceExpression;
import io.kite.Frontend.Parser.Expressions.TypeExpression;
import io.kite.Frontend.Parser.Program;
import lombok.Data;

/**
 * Statements do not return a values as opposed to expressions
 * example:
 * var x = 10 -> does not return a value, most repl return undefined
 * x = 10 -> no longer a statement is an assignment expression which returns 10
 * <p>
 * <p>
 * Statement
 * : ExpressionStatement
 * | BlockStatement
 * | EmptyStatement
 * | IfStatement
 * | WhileStatement
 * | ForStatement
 * | FunctionDeclaration
 * ;
 */
@Data
public abstract sealed class Statement
        permits ModuleExpression, ResourceExpression, TypeExpression,
        Program, EmptyStatement, ExpressionStatement, ForStatement,
        FunctionDeclaration, IfStatement, InitStatement, ReturnStatement,
        SchemaDeclaration, ValStatement, VarStatement, WhileStatement {

}
