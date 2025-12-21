package cloud.kitelang.execution.inputs;

import cloud.kitelang.syntax.ast.expressions.InputDeclaration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class InputsFilesResolver extends InputResolver {
    static final String INPUTS_DEFAULTS_KITE = "inputs.default.kite";
    static final String INPUTS_ENV_DEFAULTS_KITE = "inputs.%s.default.kite";
    private final Map<String, String> inputs;
    private boolean wasRead = false;

    public InputsFilesResolver() {
        inputs = new HashMap<>();
    }

    /**
     * Used for testing.
     */
    public static void writeToDefaults(Map<String, Object> values) {
        writeToDefaults(values, false);
    }

    public static void writeToDefaults(Map<String, Object> values, boolean stableOrder) {
        Path path = Path.of(INPUTS_DEFAULTS_KITE);
        try (var bw = Files.newBufferedWriter(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {

            // choose order
            var it = getEntryIterable(values, stableOrder);

            for (var entry : it) {
                bw.write(entry.getKey());
                bw.write(" = ");
                bw.write(toKiteLiteral(entry.getValue()));
                bw.write(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static @NotNull Iterable<Map.Entry<String, Object>> getEntryIterable(Map<String, Object> values, boolean stableOrder) {
        if (stableOrder) {
            var list = new ArrayList<>(values.entrySet());
            list.sort(Map.Entry.comparingByKey());
            return list;
        } else {
            return values.entrySet();
        }
    }

    private static String toKiteLiteral(Object v) {
        if (v == null) return "null";

        // primitives & wrappers (include BigInteger/BigDecimal)
        if (v instanceof Boolean || v instanceof Byte || v instanceof Short
            || v instanceof Integer || v instanceof Long
            || v instanceof Float || v instanceof Double
            || v instanceof BigInteger || v instanceof BigDecimal) {
            // If you don't want NaN/Infinity as bare tokens, quote them here.
            return v.toString();
        }

        // Pattern-matching switch for common cases
        switch (v) {
            case CharSequence charSequence -> {
                String string = charSequence.toString();
                if (StringUtils.isBlank(charSequence)) {
                    return ""; // results in: key =
                }
                // If already looks like a structured value, return as-is
                String trim = StringUtils.trim(string);
                if ((trim.startsWith("[") && trim.endsWith("]"))
                    || (trim.startsWith("{") && trim.endsWith("}"))) {
                    return string;
                }
                return quote(string);
            }
            case Character ch -> {
                return quote(ch.toString());
            }
            case Enum<?> en -> {
                return quote(en.name());
            }
            case Optional<?> opt -> {
                return opt.map(InputsFilesResolver::toKiteLiteral).orElse("null");
            }
            case OptionalInt opt -> {
                return opt.isPresent() ? Integer.toString(opt.getAsInt()) : "null";
            }
            case OptionalLong opt -> {
                return opt.isPresent() ? Long.toString(opt.getAsLong()) : "null";
            }
            case OptionalDouble opt -> {
                return opt.isPresent() ? Double.toString(opt.getAsDouble()) : "null";
            }
            case Collection<?> c -> {
                if (c instanceof Set<?>) {
                    var list = c.stream()
                            .map(InputsFilesResolver::toKiteLiteral)
                            .sorted()
                            .collect(Collectors.toCollection(() -> new ArrayList<>(c.size())));
                    return "[" + String.join(", ", list) + "]";
                } else {
                    return joinListlike(c);
                }
            }
            case Map<?, ?> map -> {
                return "{" + String.join(", ", map.entrySet().stream().map(e -> Map.entry(toKiteLiteral(e.getKey()), toKiteLiteral(e.getValue()))).sorted(Map.Entry.comparingByKey()).toList().stream().map(e -> e.getKey() + ": " + e.getValue()).toList()) + "}";
            }
            case Object a when a.getClass().isArray() -> {
                int n = Array.getLength(a);
                List<Object> tmp = new ArrayList<>(n);
                for (int i = 0; i < n; i++) tmp.add(Array.get(a, i));
                return joinListlike(tmp);
            }
            default -> { // fallback: quote to be safe
                return quote(v.toString());
            }
        }
    }

    private static String joinListlike(Iterable<?> it) {
        StringBuilder sb = new StringBuilder(32);
        sb.append('[');
        boolean first = true;
        for (Object o : it) {
            if (!first) sb.append(", ");
            sb.append(toKiteLiteral(o));
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }

    private static String formatMapKey(String k) {
        return k.matches("[A-Za-z_][A-Za-z0-9_]*") ? k : quote(k);
    }

    private static String quote(String s) {
        String escaped = escapeIfNeeded(s);
        return "\"" + escape(escaped) + "\"";
    }

    private static String escapeIfNeeded(String s) {
        int len = s.length();
        // First pass: detect if escaping is necessary
        boolean needs = false;
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            if (ch == '\\' || ch == '"' || ch < 0x20) {
                needs = true;
                break;
            }
        }
        if (!needs) return s;

        // Second pass: build escaped
        return escape(s);
    }

    // Minimal JSON-style escaping for safety
    private static String escape(String s) {
        var sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
                    else sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static void deleteDefaults() {
        try {
            Files.deleteIfExists(Path.of(InputsFilesResolver.INPUTS_DEFAULTS_KITE));
        } catch (IOException ignored) {
        }
    }

    @Override
    String resolve(InputDeclaration input, Object previousValue) {
        ensureFilesRead();
        return inputs.get(input.name());
    }

    @Override
    String resolve(String qualifiedName, InputDeclaration input, Object previousValue) {
        ensureFilesRead();
        return inputs.get(qualifiedName);
    }

    private void ensureFilesRead() {
        if (!wasRead) {
            readFileProperty(Paths.get(INPUTS_DEFAULTS_KITE));

            // read environment specific file. env can be specified using KITE_ENV variable.
            Optional.ofNullable(System.getenv(EnvVariablesConstants.KITE_PROFILE))
                    .ifPresent(env -> readFileProperty(Paths.get(INPUTS_ENV_DEFAULTS_KITE.formatted(env))));
            wasRead = true;
        }
    }

    private void readFileProperty(Path file) {
        try (var stream = Files.lines(file)) {
            stream.forEach(line -> {
                var input = line.split("=");
                if (input.length == 2) {
                    inputs.put(StringUtils.trim(input[0]), StringUtils.trim(input[1]).replaceAll("^['\"]|['\"]$", ""));
                }
            });
        } catch (IOException e) {
            log.info("File not present file: {}", file.toAbsolutePath());
        }
    }

}
