package cloud.kitelang.semantics.types;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
public final class FunType extends Type {
    private List<Type> params;

    private Type returnType;

    public FunType(Collection<Type> params, Type returnType) {
        super();
        this.params = List.copyOf(params);
        this.returnType = returnType;
        setValue(name());
    }

    public static FunType fun(Collection<Type> params, Type type) {
        return new FunType(params, type);
    }

    public static FunType fun(Type returnType, Type... params) {
        return new FunType(List.of(params), returnType);
    }

    public static FunType fun(@NotBlank String symbol) {
        var funSplit = StringUtils.split(symbol, "->");
        Type returnType = null;
        Collection<Type> paramsType = new ArrayList<>();
        if (funSplit.length == 2) {
            returnType = TypeFactory.fromString(funSplit[1]);
            paramsType = typesBetweenParantheses(funSplit[0]);
        } else if (funSplit.length == 1) {
            paramsType = typesBetweenParantheses(funSplit[0]);
        }
        return new FunType(paramsType, returnType);
    }

    private static List<Type> typesBetweenParantheses(String funSplit) {
        String substring = StringUtils.substringBetween(funSplit, "(", ")");
        if (substring.isEmpty()) {
            return List.of();
        }
        var split = substring.split(",");
        return Arrays.stream(split)
                .map(TypeFactory::fromString)
                .collect(Collectors.toList());
    }

    /**
     * Changing the return type must change the function signature because lambdas can infer the return type from the body
     * example: (x:Number)-> x+1 => we don't want to specify the return type since it's an easy lambda
     */
    public void setReturnType(Type returnType) {
        this.returnType = returnType;
        setValue(null);
        setValue(name());
    }

    public String name() {
        if (this.getValue() == null) {
            var value = "fun(" + StringUtils.join(params, ",") + ")" + returnType();
            setValue(value);
        }
        return getValue();
    }

    private @NotNull String returnType() {
        if (returnType == null) {
            return "";
        }
        return this.returnType.hasValue() ? ":" + this.returnType.getValue() : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof FunType funType)) return false;

        return new EqualsBuilder()
                .append(getValue(), funType.getValue())
                .append(getParams(), funType.getParams())
                .append(getReturnType(), funType.getReturnType())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getValue())
                .append(getParams())
                .append(getReturnType())
                .toHashCode();
    }

    public String getName() {
        return name();
    }

}
