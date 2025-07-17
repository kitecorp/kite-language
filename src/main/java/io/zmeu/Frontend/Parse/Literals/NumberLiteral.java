package io.zmeu.Frontend.Parse.Literals;

import io.zmeu.Frontend.Parser.Expressions.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.text.DecimalFormat;

/*
 * NumericLiteral
 *      : NUMBER
 *      ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NumberLiteral extends Literal {
    private static DecimalFormat df = new DecimalFormat("#######.#######");
    private Number value;

    private NumberLiteral() {
    }

    private NumberLiteral(double value) {
        setNumber(value);
    }

    private NumberLiteral(float value) {
        setNumber(value);
    }

    private NumberLiteral(int value) {
        setNumber(value);
    }

    private void setNumber(float value) {
        setNumber(Double.parseDouble(String.valueOf(value)));
    }

    private void setNumber(double value) {
        this.value = Double.valueOf(df.format(value));
    }

    private void setNumber(int value) {
        this.value = value;
    }

    private NumberLiteral(String value) {
        setValue(value);
    }

    private NumberLiteral(Object value) {
        if (value instanceof String s) {
            setValue(s);
        } else if (value instanceof Integer i) {
            setNumber(i);
        } else if (value instanceof Double i) {
            setNumber(i);
        } else if (value instanceof Float i) {
            setNumber(i);
        }
    }

    public static NumberLiteral number(Object value) {
        return new NumberLiteral(value);
    }

    public static Expression of(int value) {
        return new NumberLiteral(value);
    }

    public static NumberLiteral number(int value) {
        return new NumberLiteral(value);
    }

    public static NumberLiteral number(double value) {
        return new NumberLiteral(value);
    }

    public static NumberLiteral number(float value) {
        return new NumberLiteral(value);
    }

    public static NumberLiteral of(float value) {
        return new NumberLiteral(value);
    }

    public static NumberLiteral of(double value) {
        return new NumberLiteral(value);
    }

    private void setValue(String value) {
        if (value.indexOf('.') != -1) { // string contains . => is a float/double
            this.value = Double.parseDouble(value);
        } else {
            this.value = Integer.parseInt(value);
        }
    }

    @Override
    public Object getVal() {
        return value;
    }

}
