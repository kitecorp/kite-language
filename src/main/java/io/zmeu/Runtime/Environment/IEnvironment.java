package io.zmeu.Runtime.Environment;

import io.zmeu.Frontend.Parse.Literals.Identifier;
import org.jetbrains.annotations.Nullable;

public interface IEnvironment<T> {
    T assign(String varName, T value);

    T lookup(@Nullable String varName);

    T lookup(@Nullable T varName);

    IEnvironment<T> getParent();

    /**
     * Return the environment after going through n parents
     */
    default IEnvironment<T> ancestor(Integer hops) {
        IEnvironment<T> environment = this;
        if (hops == null) {
            // if number of hops is not defined we are not in a local scope so we try to find
            // the variable in the global scope. Else we know exactly how many parents we need to go up the chain
            for (var parent = getParent(); parent != null; parent = parent.getParent()){
                environment = parent;
            }
            return environment;
        }
        for (int i = 0; i < hops; i++) {
            var parent1 = environment.getParent();
            if (parent1 == null) break;

            environment = parent1;
        }
        return environment;
    }

    @Nullable T get(String key);

    default T init(Identifier name, T value) {
        return init(name.string(), value);
    }

    T init(String name, Object value);

    default T assign(String symbol, T right, Integer hops) {
        return ancestor(hops).assign(symbol, right);
    }

    default T lookup(String symbol, Integer hops) {
        return ancestor(hops).lookup(symbol);
    }

}
