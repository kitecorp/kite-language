package io.kite.Runtime.Inputs;

import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EnvResolver extends InputResolver {
    private final Map<String, Object> envVariables;

    public EnvResolver() {
        this.envVariables = captureSystemEnv();
    }

    public EnvResolver(Map<String, Object> overrides) {
        this.envVariables = overrides;
    }

    /**
     * Convert ENV keys to your internal keys (e.g., REGION -> region, CFG__ENV -> cfg.env).
     */
    public static @Nullable String normalizeKey(String envKey) {
        String k = envKey.substring(EnvVariablesConstants.PREFIX.length());

        k = k.toLowerCase(Locale.ROOT); // Windows env is case-insensitive; use ROOT to avoid Turkish-i issues

        // optional: double underscore -> dot path
        k = k.replace("__", ".");
        return k;
    }

    public static Map<String, Object> captureSystemEnv() {
        Map<String, Object> out = new HashMap<>();
        for (var e : System.getenv().entrySet()) {
            var key = e.getKey();
            if (!key.startsWith(EnvVariablesConstants.PREFIX)) {
                continue;
            }

            var normalizedKey = normalizeKey(key);
            if (e.getValue() != null) {
                out.put(normalizedKey, e.getValue());
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
    @Nullable String resolve(InputDeclaration key, Object previousValue) {
        Object o = envVariables.get(key.name());
        if (o == null) return null;

        return o.toString();
    }
}
