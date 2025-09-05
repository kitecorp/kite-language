package io.kite.Runtime.Inputs;

import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@Log4j2
public class InputsDefaultsFilesFinder extends InputResolver {
    static final String INPUTS_DEFAULTS_KITE = "inputs.default.kite";
    static final String INPUTS_ENV_DEFAULTS_KITE = "inputs.%s.default.kite";
    private final Map<String, String> inputs;
    private boolean wasRead = false;

    public InputsDefaultsFilesFinder() {
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
                String s = charSequence.toString();
                String trim = StringUtils.trim(s);
                if (trim.isEmpty()) {
                    if (!s.isEmpty()) {
                        System.err.println("Warning: value looks like only whitespace/invisible characters; writing empty literal.");
                    }
                    return ""; // results in: key =
                }
                // If already looks like a structured value, return as-is
                if ((trim.startsWith("[") && trim.endsWith("]"))
                    || (trim.startsWith("{") && trim.endsWith("}"))) {
                    return s;
                }
                return quote(s);
            }
            case Character ch -> {
                return quote(ch.toString());
            }
            case Enum<?> en -> {
                return quote(en.name());
            }
            case Optional<?> opt -> {
                return opt.map(InputsDefaultsFilesFinder::toKiteLiteral).orElse("null");
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
            default -> { /* fall through */ }
        }

        // UUID / Path / temporal -> string literal
        if (v instanceof java.util.UUID || v instanceof java.nio.file.Path || v instanceof java.time.temporal.TemporalAccessor) {
            return quote(v.toString());
        }

        // arrays (primitive/object)
        if (v.getClass().isArray()) {
            int n = java.lang.reflect.Array.getLength(v);
            java.util.List<String> items = new java.util.ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                items.add(toKiteLiteral(java.lang.reflect.Array.get(v, i)));
            }
            return "[" + String.join(", ", items) + "]";
        }

        // collections: keep List order; sort Set for determinism
        if (v instanceof java.util.Collection<?> c) {
            String items = c.stream()
                    .map(InputsDefaultsFilesFinder::toKiteLiteral)
                    .collect(java.util.stream.Collectors.joining(", "));
            return "[" + items + "]";
        }

        // maps (stringify keys; bare if identifier-like, else quoted)
        if (v instanceof java.util.Map<?, ?> m) {
            var entries = m.entrySet().stream()
                    .map(e -> Map.entry(String.valueOf(e.getKey()), e.getValue()))
                    .sorted(Map.Entry.comparingByKey())
                    .toList();

            String inner = entries.stream()
                    .map(e -> formatMapKey(e.getKey()) + ": " + toKiteLiteral(e.getValue()))
                    .collect(java.util.stream.Collectors.joining(", "));
            return "{ " + inner + " }";
        }

        // fallback: quote to be safe
        return quote(v.toString());
    }

    private static String formatMapKey(String k) {
        return k.matches("[A-Za-z_][A-Za-z0-9_]*") ? k : quote(k);
    }

    private static String quote(String s) {
        return "\"" + escape(s) + "\"";
    }

    // Minimal JSON-style escaping for safety
    private static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
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
                    if (ch < 0x20) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    public static void deleteDefaults() {
        try {
            Files.deleteIfExists(Path.of(InputsDefaultsFilesFinder.INPUTS_DEFAULTS_KITE));
        } catch (IOException e) {
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

    @Override
    String resolve(InputDeclaration input, String previousValue) {
        // read default file
        if (!wasRead) {
            readFileProperty(Paths.get(INPUTS_DEFAULTS_KITE));

            // read environment specific file. env can be specified using KITE_ENV variable.
            Optional.ofNullable(System.getenv(EnvVariablesConstants.KITE_ENV))
                    .ifPresent(env -> readFileProperty(Paths.get(INPUTS_ENV_DEFAULTS_KITE.formatted(env))));
            wasRead = true;
        }
        return inputs.get(input.name());
    }

}
