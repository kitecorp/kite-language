package io.kite.syntax.ast.expressions;

import io.kite.semantics.types.Type;
import io.kite.syntax.ast.statements.BlockExpression;
import io.kite.syntax.ast.statements.LambdaExpression;
import io.kite.syntax.parser.literals.Identifier;
import io.kite.syntax.parser.literals.Literal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public sealed abstract class Expression implements Callstack
        permits Identifier, Literal, AnnotationDeclaration, ArrayExpression, AssignmentExpression, BinaryExpression, CallExpression, ErrorExpression, GroupExpression, LogicalExpression, MemberExpression, ObjectExpression, ThisExpression, UnaryExpression, ValDeclaration, VarDeclaration, BlockExpression, LambdaExpression, Type {


}
