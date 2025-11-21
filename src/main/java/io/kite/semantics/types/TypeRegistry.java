package io.kite.semantics.types;

import java.util.HashMap;
import java.util.Map;

public class TypeRegistry<T extends Type> {
    private final Map<String, T> store ;

    public TypeRegistry() {
        store = new HashMap<>();
    }

    public T getType(String funName) {
        return store.get(funName);
    }

    public T setType(String funName, T funType) {
        return store.put(funName, funType);
    }

}
