package cloud.kitelang.semantics.types;

import java.util.HashMap;
import java.util.Map;

public class FunStore {
    private final Map<String, FunType> store = new HashMap<>();
    private static FunStore instance;

    public static FunStore getInstance() {
        if (instance == null) {
            instance = new FunStore();
        }
        return instance;
    }

    public FunType getFun(String funName) {
        return store.get(funName);
    }

    public FunType setFun(String funName, FunType funType) {
        return store.put(funName, funType);
    }

    public FunType setFun(FunType funType) {
        return store.put(funType.name(), funType);
    }

    public void addFun(String funName, FunType funType) {
        if (getFun(funName) != null) {
            throw new RuntimeException("Fun '" + funName + "' already exists");
        }
        setFun(funName, funType);
    }
}
