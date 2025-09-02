package io.kite.Runtime.Inputs;

import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Runtime.Environment.Environment;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EnvResolver extends InputResolver {
    private static final String PREFIX = "kite_input_";
    private final Map<String, String> envVariables;

    public EnvResolver(Environment<Object> inputs) {
        super(inputs);
        this.envVariables = captureSystemEnv();
    }

    public EnvResolver(Environment<Object> inputs, Map<String, String> overrides) {
        super(inputs);
        this.envVariables = overrides;
    }

    /**
     * Convert ENV keys to your internal keys (e.g., REGION -> region, CFG__ENV -> cfg.env).
     */
    public static String normalizeKey(String envKey) {
        String k = envKey.substring(PREFIX.length());

        k = k.toLowerCase(Locale.ROOT); // Windows env is case-insensitive; use ROOT to avoid Turkish-i issues

        // optional: double underscore -> dot path
        k = k.replace("__", ".");
        return k;
    }

    public static Map<String, String> captureSystemEnv() {
        Map<String, String> out = new HashMap<>();
        for (var e : System.getenv().entrySet()) {
            if (e.getKey().startsWith(PREFIX) && e.getValue() != null) {
                out.put(normalizeKey(e.getKey()), e.getValue());
            }
        }
        return out;
    }

    /**
     * Merge base snapshot with test overrides; overrides win.
     */
    public static Map<String, String> overlay(Map<String, String> base, Map<String, String> overrides) {
        Map<String, String> merged = new LinkedHashMap<>(base);
        merged.putAll(overrides); // higher precedence
        return Collections.unmodifiableMap(merged);
    }

    @Override
    public @Nullable String resolve(InputDeclaration key) {
        return envVariables.get(key.name());
    }
}
