package io.kite.Runtime.Environment;

import io.kite.Runtime.Values.ResourceValue;
import io.kite.Runtime.exceptions.DeclarationExistsException;
import io.kite.Runtime.exceptions.NotFoundException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a lexical scope for variable storage and lookup.
 * <p>
 * IMPORTANT: Environment equality is based on:
 * - variables (Map content)
 * - name (String)
 * BUT NOT parent (excluded from equals/hashCode)
 * <p>
 * When comparing Environments in tests, ensure both 'variables' AND 'name' match!
 */
@Log4j2
@Data
public class Environment<T> implements IEnvironment<T> {
    @Nullable
    @Getter
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Environment<T> parent;
    @Getter
    private final Map<String, T> variables;

    private String name;

    public Environment(@Nullable Environment<T> parent) {
        this.parent = parent;
        this.variables = new HashMap<>(8);
    }

    public Environment(String name, @Nullable Environment<T> parent) {
        this.parent = parent;
        this.variables = new HashMap<>(8);
        this.name = name;
    }

    public Environment(@Nullable Environment<T> parent, Map<String, T> variables) {
        this(parent);
        this.variables.putAll(variables);
    }

    public Environment(@Nullable Environment<T> parent, ResourceValue variables) {
        this(parent);
        for (Field field : variables.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                this.variables.put(field.getName(), (T) field.get(field.getName()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Environment(Map<String, T> variables) {
        this.parent = null;
        this.variables = variables;
        this.name = "global";
    }

    public Environment() {
        this(new HashMap<>());
    }

    public Environment(String name) {
        this(new HashMap<>(1));
        this.name = name;
    }

    public Environment(String name, Environment<T> env, Map<String, T> variables) {
        this(env, variables);
        this.name = name;
    }

    public static <T> Environment<T> copyOfVariables(Environment<T> environment) {
        return new Environment<>(environment, environment.variables);
    }

    /**
     * Factory method to create an Environment with a name and variables (no parent).
     * Useful for creating test fixtures.
     *
     * @param name      The environment name
     * @param variables The variables map
     * @return A new Environment instance
     */
    public static <T> Environment<T> of(String name, Map<String, T> variables) {
        return new Environment<>(name, null, variables);
    }

    /**
     * Factory method to create an Environment with only variables (name defaults to "global").
     * Useful for simple test cases.
     *
     * @param variables The variables map
     * @return A new Environment instance with name="global"
     */
    public static <T> Environment<T> of(Map<String, T> variables) {
        return new Environment<>(variables);
    }

    /**
     * Factory method to create an Environment with a name, parent, and variables.
     * Useful for nested scopes in tests.
     *
     * @param name      The environment name
     * @param parent    The parent environment
     * @param variables The variables map
     * @return A new Environment instance
     */
    public static <T> Environment<T> of(String name, Environment<T> parent, Map<String, T> variables) {
        return new Environment<>(name, parent, variables);
    }

    /**
     * Declare a new variable with given name and value.
     * var a = 2
     * var b = 3
     */
    @Override
    public T init(String name, Object value) {
        if (variables.containsKey(name)) {
            throw new DeclarationExistsException(name);
        }
        this.put(name, (T) value);
        return (T) value;
    }

    /**
     * Assign a value to an existing variable
     * x = 10
     */
    @Override
    public T assign(String varName, T value) {
        var env = this.resolve(varName);
        env.put(varName, value);
        return value;
    }

    public T initOrAssign(String varName, T value) {
        if (hasVar(varName)) {
            return assign(varName, value);
        } else {
            this.put(varName, value);
            return value;
        }
    }

    @Override
    @Nullable
    public T lookup(@Nullable String varName) {
        if (varName == null) {
            varName = "null";
        }
        return resolve(varName) // search the scope
                .get(varName); // return the value
    }

    @Override
    public T lookup(@Nullable T varName) {
        return lookup(varName);
    }

    /**
     * Search in the current scope for a variable, if found, return it; if not found, search in the parent scope
     *
     * @param symbol
     * @return
     */
    private Environment<T> resolve(String symbol, String error) {
        if (variables.containsKey(symbol)) {
            return this;
        }
        if (parent == null) {
            throw new NotFoundException(error, symbol);
        }
        return this.parent.resolve(symbol, error);
    }

    private Environment<T> resolve(String symbol) {
        return resolve(symbol, "Variable not found: ");
    }

    private void put(String key, T value) {
        this.variables.put(key, value);
    }

    @Override
    @Nullable
    public T get(String key) {
        return this.variables.get(key);
    }

    public boolean containsKey(String key) {
        return variables.containsKey(key);
    }

    public boolean lookupKey(String key) {
        if (containsKey(key)) {
            return true;
        }
        if (parent == null) {
            return false;
        }
        return parent.lookupKey(key);
    }

    @Nullable
    public T lookup(String symbol, String error) {
        return resolve(symbol, error) // search the scope
                .get(symbol);
    }

    public boolean hasVar(String symbol) {
        return getVariables().containsKey(symbol);
    }

    public void remove(String key) {
        variables.remove(key);
    }

    public int size() {
        return variables.size();
    }
}
