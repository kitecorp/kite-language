package io.kite.runtime.inputs;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple parser for JSON and Kit objects and array input
 */
public final class InputParser {
    private final String s;
    private int i = 0;

    private InputParser(String s) {
        this.s = s;
    }

    /**
     * Converts relaxed form to strict JSON (no comments support).
     */
    static String normalize(String s) {
        String out = s.trim();
        // 1) Quote bare keys after '{' or ','  →  "key":
        out = out.replaceAll("(?<=\\{|,)\\s*([A-Za-z_][A-Za-z0-9_\\-.]*)\\s*:", "\"$1\":");
        // 2) Convert single-quoted strings to double-quoted strings
        out = out.replaceAll("'((?:\\\\'|[^'])*?)'", "\"$1\"");
        // 3) Remove trailing commas before '}' or ']'
        out = out.replaceAll(",\\s*(?=[}\\]])", "");
        return out;
    }

    /**
     * Parse any JSON value (object/array/string/number/bool/null).
     */
    public static Object parse(String json) {
        return new InputParser(normalize(json)).parseValue();
    }

    /**
     * Parse a JSON object and return Map<String,Object>.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object v = parse(normalize(json));
        if (!(v instanceof Map)) throw new IllegalArgumentException("Root is not an object");
        return (Map<String, Object>) v;
    }

    /**
     * Parse a JSON array and return List<Object>.
     */
    @SuppressWarnings("unchecked")
    public static List<Object> parseArray(String json) {
        Object v = parse(normalize(json));
        if (!(v instanceof List)) throw new IllegalArgumentException("Root is not an array");
        return (List<Object>) v;
    }

    // ---- core ----
    private Object parseValue() {
        skipWs();
        if (eof()) error("EOF");
        char c = s.charAt(i);
        switch (c) {
            case '{':
                return parseObject0();
            case '[':
                return parseArray0();
            case '"':
                return parseString();
            case 't':
                return literal("true", Boolean.TRUE);
            case 'f':
                return literal("false", Boolean.FALSE);
            case 'n':
                return literal("null", null);
            default:
                if (c == '-' || isDigit(c)) return parseNumber();
                error("Unexpected char: " + c);
                return null; // unreachable
        }
    }

    private Map<String, Object> parseObject0() {
        expect('{');
        skipWs();
        Map<String, Object> m = new LinkedHashMap<>();
        if (peek('}')) {
            i++;
            return m;
        }
        while (true) {
            skipWs();
            if (!peek('"')) error("Expected string key");
            String key = parseString();
            skipWs();
            expect(':');
            skipWs();
            Object val = parseValue();
            m.put(key, val);
            skipWs();
            if (peek(',')) {
                i++;
                continue;
            }
            if (peek('}')) {
                i++;
                break;
            }
            error("Expected ',' or '}'");
        }
        return m;
    }

    private List<Object> parseArray0() {
        expect('[');
        skipWs();
        List<Object> a = new ArrayList<>();
        if (peek(']')) {
            i++;
            return a;
        }
        while (true) {
            a.add(parseValue());
            skipWs();
            if (peek(',')) {
                i++;
                continue;
            }
            if (peek(']')) {
                i++;
                break;
            }
            error("Expected ',' or ']'");
        }
        return a;
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (!eof()) {
            char c = s.charAt(i++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                if (eof()) error("Bad escape");
                char e = s.charAt(i++);
                switch (e) {
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '/':
                        sb.append('/');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'u':
                        if (i + 4 > s.length()) error("Bad \\u escape");
                        sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        i += 4;
                        break;
                    default:
                        error("Bad escape: \\" + e);
                }
            } else {
                sb.append(c);
            }
        }
        error("Unterminated string");
        return null;
    }

    private Object parseNumber() {
        int start = i;
        if (s.charAt(i) == '-') i++;
        if (i >= s.length()) error("Bad number");
        if (s.charAt(i) == '0') {
            i++;
        } else {
            if (!isDigit(s.charAt(i))) error("Bad number");
            while (i < s.length() && isDigit(s.charAt(i))) i++;
        }
        boolean floating = false;
        if (i < s.length() && s.charAt(i) == '.') {
            floating = true;
            i++;
            if (i >= s.length() || !isDigit(s.charAt(i))) error("Bad fraction");
            while (i < s.length() && isDigit(s.charAt(i))) i++;
        }
        if (i < s.length() && (s.charAt(i) == 'e' || s.charAt(i) == 'E')) {
            floating = true;
            i++;
            if (i < s.length() && (s.charAt(i) == '+' || s.charAt(i) == '-')) i++;
            if (i >= s.length() || !isDigit(s.charAt(i))) error("Bad exponent");
            while (i < s.length() && isDigit(s.charAt(i))) i++;
        }
        String num = s.substring(start, i);
        if (floating) return Double.parseDouble(num);
        try {
            return Long.parseLong(num);
        } catch (NumberFormatException e) {
            return new BigInteger(num);
        }
    }

    private Object literal(String word, Object val) {
        if (!s.regionMatches(i, word, 0, word.length())) error("Expected " + word);
        i += word.length();
        return val;
    }

    // ---- helpers ----
    private void skipWs() {
        while (!eof() && " \t\r\n".indexOf(s.charAt(i)) >= 0) i++;
    }

    private boolean eof() {
        return i >= s.length();
    }

    private boolean peek(char c) {
        return !eof() && s.charAt(i) == c;
    }

    private void expect(char c) {
        if (eof() || s.charAt(i) != c) error("Expected '" + c + "'");
        i++;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void error(String msg) {
        throw new IllegalArgumentException(msg + " at pos " + i);
    }
}