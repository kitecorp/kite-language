package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.ArrayExpression;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Frontend.Parser.Expressions.OutputDeclaration;
import io.kite.Runtime.Interpreter;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.SystemType;
import io.kite.TypeChecker.Types.Type;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ValidateDecorator extends DecoratorInterpreter {
    // Preset patterns are fully anchored and intended for FULL matches.
    private static final Map<String, String> PRESETS = Map.ofEntries(
            // --- your existing ones ---
            Map.entry("dns_label", "^[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$"),
            Map.entry("rfc1123", "^(?=.{1,253}$)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)(?:\\.(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?))*$"),
            Map.entry("kebab", "^[a-z0-9]+(?:-[a-z0-9]+)*$"),

            // --- core identifiers ---
            Map.entry("identifier", "^[A-Za-z_][A-Za-z0-9_]*$"),
            Map.entry("lower_snake", "^[a-z][a-z0-9_]*$"),
            Map.entry("upper_snake", "^[A-Z][A-Z0-9_]*$"),
            Map.entry("camel", "^[a-z]+(?:[A-Z][a-z0-9]*)*$"),
            Map.entry("pascal", "^[A-Z][A-Za-z0-9]*$"),

            // --- host / url / path ---
            // Note: 'hostname' allows upper/lower; use flags="i" if you prefer lowercase-only behavior.
            Map.entry("hostname", "^(?=.{1,253}$)(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)(?:\\.(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?))*$"),
            Map.entry("uri_path", "^/([A-Za-z0-9._~!$&'()*+,;=:@/\\-]*)$"),

            // --- tokens / ids ---
            Map.entry("uuid", "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"),
            Map.entry("hex", "^[0-9a-fA-F]+$"),
            Map.entry("sha256", "^[A-Fa-f0-9]{64}$"),
            Map.entry("base64", "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$"),

            // --- user/app inputs ---
            Map.entry("email", "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"),
            Map.entry("url", "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$"),
            Map.entry("ipv4", "^(25[0-5]|2[0-4]\\d|[01]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[01]?\\d?\\d)){3}$"),
            // IPv6 (compressed/expanded forms). Yes, it’s large; it’s the practical standard pattern.
            Map.entry("ipv6",
                    "^((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|" +
                    "(([0-9A-Fa-f]{1,4}:){1,7}:)|" +
                    "(([0-9A-Fa-f]{1,4}:){1,6}:[0-9A-Fa-f]{1,4})|" +
                    "(([0-9A-Fa-f]{1,4}:){1,5}(:[0-9A-Fa-f]{1,4}){1,2})|" +
                    "(([0-9A-Fa-f]{1,4}:){1,4}(:[0-9A-Fa-f]{1,4}){1,3})|" +
                    "(([0-9A-Fa-f]{1,4}:){1,3}(:[0-9A-Fa-f]{1,4}){1,4})|" +
                    "(([0-9A-Fa-f]{1,4}:){1,2}(:[0-9A-Fa-f]{1,4}){1,5})|" +
                    "([0-9A-Fa-f]{1,4}:((:[0-9A-Fa-f]{1,4}){1,6}))|" +
                    "(:(:([0-9A-Fa-f]{1,4}){1,7}|:))|" +
                    "(fe80:(:[0-9A-Fa-f]{0,4}){0,4}%[0-9A-Za-z]{1,})|" +
                    "(::(ffff(:0{1,4}){0,1}:){0,1}" +
                    "((25[0-5]|(2[0-4]|1{0,1}[0-9])?[0-9])\\.){3,3}" +
                    "(25[0-5]|(2[0-4]|1{0,1}[0-9])?[0-9]))|" +
                    "(([0-9A-Fa-f]{1,4}:){1,4}:" +
                    "((25[0-5]|(2[0-4]|1{0,1}[0-9])?[0-9])\\.){3,3}" +
                    "(25[0-5]|(2[0-4]|1{0,1}[0-9])?[0-9])))$"
            ),

            // --- cloud / infra shapes ---
            Map.entry("arn", "^arn:(aws|aws-cn|aws-us-gov):[a-z0-9-]+:[a-z0-9-]*:\\d{12}:.+$"),
            Map.entry("gcp_resource", "^projects/[A-Za-z0-9._-]+/.+$"),
            Map.entry("azure_id", "^/subscriptions/[0-9a-fA-F-]+/resourceGroups/.+$"),
            Map.entry("s3_bucket", "^[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]$"),
            Map.entry("docker_image", "^[a-z0-9]+(?:[._-][a-z0-9]+)*(?:/[a-z0-9]+(?:[._-][a-z0-9]+)*)*$"),
            Map.entry("semver", "^v?(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?(?:\\+[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?$")
    );
    @Getter
    private final Map<String, Pattern> CACHE = new HashMap<>();
    private final Interpreter interpreter;

    public ValidateDecorator(Interpreter interpreter) {
        super("validate");
        this.interpreter = interpreter;
    }

    private static boolean isStringOrStringArray(Type t) {
        return t.getKind() == SystemType.STRING || t.getKind() == SystemType.ARRAY;
    }

    private static Object getFlagsArg(AnnotationDeclaration decl) {
        return decl.getStringArg("flags"); // could be string or array literal in your AST
    }

    private static int parseFlags(Object flagsArg) {
        if (flagsArg == null) return 0;
        Set<Character> flags = getCharacterSet(flagsArg);
        int bits = 0;
        for (char f : flags) {
            switch (f) {
                case 'i' -> bits |= Pattern.CASE_INSENSITIVE;
                case 'm' -> bits |= Pattern.MULTILINE;
                case 's' -> bits |= Pattern.DOTALL;
                case 'u' -> bits |= Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS;
                case 'x' -> bits |= Pattern.COMMENTS;
                default -> throw new IllegalArgumentException("Unsupported @validate flag: " + f);
            }
        }
        return bits;
    }

    private static @NotNull Set<Character> getCharacterSet(Object flagsArg) {
        Set<Character> flags = new LinkedHashSet<>();
        switch (flagsArg) {
            case StringLiteral s -> {
                for (char c : s.getValue().toCharArray()) flags.add(c);
            }
            case String s -> {
                for (char c : s.toCharArray()) flags.add(c);
            }
            case ArrayExpression arr -> {
                for (var e : arr.getItems()) {
                    if (e instanceof StringLiteral sl && sl.getValue().length() == 1) {
                        flags.add(sl.getValue().charAt(0));
                    } else {
                        throw new IllegalArgumentException("@validate flags must be single-letter strings");
                    }
                }
            }
            default -> throw new IllegalArgumentException("@validate flags must be string or string[]");
        }
        return flags;
    }

    private static void checkTargetType(AnnotationDeclaration declaration) {
        // 1) Target check at parse/typecheck time in your framework (keep here as guard)
        var target = declaration.getTarget();
        if (!(target instanceof InputDeclaration || target instanceof OutputDeclaration)) {
            throw new TypeError("@validate is only allowed on input/output");
        }
    }

    private static Pattern compileStrict(String userRegex, int flags) {
        // Enforce FULL match by wrapping, unless user already anchored both ends
        String rx = userRegex;
        boolean anchoredStart = rx.startsWith("^");
        boolean anchoredEnd = rx.endsWith("$");
        if (!anchoredStart) rx = "^" + rx;
        if (!anchoredEnd) rx = rx + "$";
        try {
            return Pattern.compile(rx, flags);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid @validate regex: " + e.getDescription(), e);
        }
    }

    private void validateValueOrArray(AnnotationDeclaration decl, Pattern p, String msg, Object value) {
        switch (value) {
            case null -> {
                return; // allow null; other decorators (e.g. @required) can enforce presence
            }
            case String s -> {
                if (!p.matcher(s).matches()) throw fail(decl, msg, s, -1);
                return;
            }
            case StringLiteral s -> {
                if (!p.matcher(s.getValue()).matches()) throw fail(decl, msg, s.getValue(), -1);
                return;
            }
            case List<?> list -> {
                validateArray(decl, p, msg, list);
                return;
            }
            case ArrayExpression arr -> {
                validateArray(decl, p, msg, arr.getItems());
                return;
            }
            default -> {
            }
        }
        // If your runtime uses custom array/value wrappers, adapt the above accordingly.
        throw new TypeError("@validate expects string or string[] value, got: " + value.getClass().getSimpleName());
    }

    private void validateArray(AnnotationDeclaration decl, Pattern p, String msg, List<?> list) {
        for (int i = 0; i < list.size(); i++) {
            Object el = list.get(i);
            if (el instanceof String s) {
                if (!p.matcher(s).matches()) throw fail(decl, msg, s, i);
            } else if (el instanceof StringLiteral s) {
                if (!p.matcher(s.getValue()).matches()) throw fail(decl, msg, s.getValue(), i);
            } else {
                throw new TypeError("@validate on string[] requires all elements be strings (index " + i + ")");
            }
        }
    }

    private RuntimeException fail(AnnotationDeclaration decl, String custom, String offending, int index) {
        String base = (custom != null && !custom.isBlank()) ? custom : "@validate failed";
        String where = (index >= 0) ? (" at index " + index) : "";
        String name = switch (decl.getTarget()) {
            case InputDeclaration declaration -> interpreter.getPrinter().visit(declaration);
            case OutputDeclaration declaration -> interpreter.getPrinter().visit(declaration);
            default -> "@validate failed";
        };

        return new IllegalArgumentException(MessageFormat.format("{0} for `{1}`{2}. Invalid value: \"{3}\"", base, name, where, offending));
    }

    @Override
    public Object execute(Interpreter interpreter, AnnotationDeclaration declaration) {
        checkTargetType(declaration);
        // 2) Args: regex (required), flags (optional), message (optional)
        String preset = declaration.getStringArg("preset");
        String regex = declaration.getStringArg("regex");
        if (StringUtils.isBlank(regex) && StringUtils.isBlank(preset)) {
            throw new IllegalArgumentException("@validate requires a non-empty 'regex' argument or a preset");
        } else if (StringUtils.isNotBlank(preset) && StringUtils.isNotBlank(regex)) {
            throw new IllegalArgumentException("@validate: use either 'preset' or 'regex', not both");
        }

        int flagBits = parseFlags(getFlagsArg(declaration)); // "i", "im", or ["i","m"]
        String message = declaration.getStringArg("message");

        // Compile or fetch cached (key = regex+'\u0000'+flagBits)
        Pattern pattern;
        if (preset != null) {
            pattern = presetPattern(preset, flagBits);
        } else {
            // User regex: enforce full match by anchoring if needed.
            pattern = CACHE.computeIfAbsent(regex + '\u0000' + flagBits, k -> compileStrict(regex, flagBits));
        }
        // 3) Type compatibility (string or string[])
        var declaredType = declaration.getTarget().targetType(); // your way of getting the type
        if (!isStringOrStringArray(declaredType)) {
            throw new TypeError("@validate applies only to string or string[]");
        }

        // 4) If initializer is a constant → early validation
        switch (declaration.getTarget()) {
            case InputDeclaration input -> validateValueOrArray(declaration, pattern, message, input.getInit());
            case OutputDeclaration output -> validateValueOrArray(declaration, pattern, message, output.getInit());
            default -> {
            }
        }
        return true;
        // 5) Register runtime validation hook for final resolved value
//        interpreter.onFinalize(target, value ->
//                validateValueOrArray(declaration, pattern, message, value));
    }

    private @NotNull Pattern presetPattern(String preset, int flagBits) {
        Pattern pattern;
        String rx = PRESETS.get(preset);
        if (rx == null) throw new IllegalArgumentException("Unknown @validate preset: " + preset);
        // Presets are complete; compile as-is with flags.
        pattern = CACHE.computeIfAbsent("preset\0" + preset + "\0" + flagBits,
                k -> compileStrict(rx, flagBits));
        return pattern;
    }

}
