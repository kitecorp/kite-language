package io.kite.frontend.parser.statements;

import io.kite.frontend.parser.Program;
import io.kite.frontend.parser.expressions.*;
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
        permits ComponentStatement, InputDeclaration, OutputDeclaration, ResourceStatement, UnionTypeStatement, Program, EmptyStatement, ExpressionStatement, ForStatement, FunctionDeclaration, IfStatement, InitStatement, ReturnStatement, SchemaDeclaration, ValStatement, VarStatement, WhileStatement {

}
