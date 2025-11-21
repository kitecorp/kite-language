package io.kite.frontend.parser.expressions;

import io.kite.frontend.parse.literals.Identifier;
import io.kite.frontend.parse.literals.Literal;
import io.kite.frontend.parser.statements.BlockExpression;
import io.kite.frontend.parser.statements.LambdaExpression;
import io.kite.typechecker.types.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public sealed abstract class Expression implements Callstack
        permits Identifier, Literal, AnnotationDeclaration, ArrayExpression, AssignmentExpression, BinaryExpression, CallExpression, ErrorExpression, GroupExpression, LogicalExpression, MemberExpression, ObjectExpression, ThisExpression, UnaryExpression, ValDeclaration, VarDeclaration, BlockExpression, LambdaExpression, Type {


}
