package io.zmeu.Frontend.Parser.Expressions;

import io.zmeu.Frontend.Parser.Literals.Identifier;
import io.zmeu.Frontend.Parser.Literals.Literal;
import io.zmeu.Frontend.Parser.Statements.BlockExpression;
import io.zmeu.Frontend.Parser.Statements.LambdaExpression;
import io.zmeu.TypeChecker.Types.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public sealed abstract class Expression permits AssignmentExpression, BinaryExpression, CallExpression, ErrorExpression, GroupExpression, LogicalExpression, MemberExpression, ObjectExpression, ThisExpression, UnaryExpression, ValDeclaration, VarDeclaration, Identifier, Literal, BlockExpression, LambdaExpression, Type {


}
