package io.kite.Frontend.visitors;

import io.kite.Frontend.Parser.Expressions.BinaryExpression;
import io.kite.Frontend.Parser.Expressions.GroupExpression;
import io.kite.Frontend.Parser.Expressions.LogicalExpression;
import io.kite.Frontend.Parser.Expressions.UnaryExpression;
import io.kite.Frontend.Parse.Literals.NumberLiteral;
import io.kite.Visitors.AstPrinter;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Log4j2
class AstPrinterTest {
    private final AstPrinter printer = new AstPrinter();

    @Test
    void numeric() {
        var res = printer.print(NumberLiteral.number("2"));
        log.warn(res);
        Assertions.assertEquals("2", res);
    }

    @Test
    void logical() {
        var res = printer.print(LogicalExpression.of(">", NumberLiteral.number("2"), NumberLiteral.number("3")));
        log.warn(res);
        Assertions.assertEquals("(> 2 3)", res);
    }

    @Test
    void group() {
        var res = printer.print(new GroupExpression(NumberLiteral.of(2)));
        log.warn(res);
        Assertions.assertEquals("(group 2)", res);
    }

    @Test
    void groupComplex() {
        var expr = BinaryExpression.binary("*",
                UnaryExpression.of("-", NumberLiteral.of(2)),
                new GroupExpression(NumberLiteral.of(3)));
        var res = printer.print(expr);
        log.warn(res);
        Assertions.assertEquals("(* (- 2) (group 3))", res);
    }

}