package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.Literal;
import io.kite.Frontend.Parser.Statements.BlockExpression;
import io.kite.Frontend.Parser.Statements.LambdaExpression;
import io.kite.TypeChecker.Types.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public sealed abstract class Expression implements Callstack
        permits Identifier, Literal,
        ArrayExpression, AssignmentExpression, BinaryExpression, CallExpression,
        ErrorExpression, GroupExpression, LogicalExpression, MemberExpression, ObjectExpression,
        ThisExpression, UnaryExpression, ValDeclaration, VarDeclaration, BlockExpression, LambdaExpression, Type {


}
