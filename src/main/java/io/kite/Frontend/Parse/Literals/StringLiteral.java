package io.kite.Frontend.Parse.Literals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * A string literal has the form of: "hello" or empty string ""
 * StringLiteral
 * : STRING
 * ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StringLiteral extends Literal {
    private String value;
    private List<String> interpolationVars;

    public StringLiteral() {
    }

    public StringLiteral(String value) {
        this();
        setValue(value);
    }

    public StringLiteral(Object value) {
        this();
        if (value instanceof String s) {
            setValue(s);
        }
    }

    public static StringLiteral of(String value) {
        return new StringLiteral(value);
    }

    public static StringLiteral string(String value) {
        return new StringLiteral(value);
    }

    private void setValue(@Nullable String value) {
        if (isBlank(value)) {
            this.value = value;
        } else {
            this.value = StringLiteralUtils.quote(value);
        }
        if (contains(this.value, '$')) {
            this.interpolationVars = StringLiteralUtils.extractNames(this.value);
        }
    }

    public String getInterpolatedString(List<String> values) {
        if (values.size() != this.interpolationVars.size()) {
            throw new IllegalArgumentException("The number of values does not match the number of interpolation variables");
        }
        var map = new HashMap<String, String>();
        for (int i = 0; i < this.interpolationVars.size(); i++) {
            String interpolationVar = this.interpolationVars.get(i);
            map.put(interpolationVar, values.get(i));
        }
        return StringLiteralUtils.replaceVariables(getValue(), map);
    }

    public boolean isInterpolated() {
        return this.interpolationVars != null && !this.interpolationVars.isEmpty();
    }

    @Override
    public Object getVal() {
        return value;
    }

}
