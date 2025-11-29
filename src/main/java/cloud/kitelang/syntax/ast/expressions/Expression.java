package cloud.kitelang.syntax.ast.expressions;

import cloud.kitelang.semantics.types.Type;
import cloud.kitelang.syntax.ast.statements.BlockExpression;
import cloud.kitelang.syntax.ast.statements.LambdaExpression;
import cloud.kitelang.syntax.literals.Identifier;
import cloud.kitelang.syntax.literals.Literal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public sealed abstract class Expression implements Callstack
        permits Identifier, Literal, AnnotationDeclaration, ArrayExpression, AssignmentExpression, BinaryExpression, CallExpression, ErrorExpression, GroupExpression, LogicalExpression, MemberExpression, ObjectExpression, StringInterpolation, ThisExpression, UnaryExpression, ValDeclaration, VarDeclaration, BlockExpression, LambdaExpression, Type {


}
