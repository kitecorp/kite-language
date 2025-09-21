package io.kite.Runtime.Decorators;

import lombok.Data;

import java.util.List;

@Data
public abstract class DecoratorInterpreter {
    private String name;

    public abstract Object execute(List<Object> args);

}
