package io.kite.syntax.ast.statements;

import io.kite.syntax.ast.Program;
import io.kite.syntax.ast.expressions.*;
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
public abstract sealed class Statement implements Callstack
        permits ComponentStatement, InputDeclaration, OutputDeclaration, ResourceStatement, UnionTypeStatement, Program, EmptyStatement, ExpressionStatement, ForStatement, FunctionDeclaration, IfStatement, ImportStatement, InitStatement, ReturnStatement, SchemaDeclaration, ValStatement, VarStatement, WhileStatement {

}
