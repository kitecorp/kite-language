package cloud.kitelang.syntax.literals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringLiteralUtils {
    private static final Pattern INTERPOLATION = Pattern.compile("\\$\\{([^}]+)\\}|\\$([A-Za-z_][A-Za-z0-9_]*)");

    /**
     * The text inside one pair of quotes around the whole value, or the value itself
     * when it is not quoted. Only a pair that opens and closes the value is removed: a
     * value holding quotes, like the JSON of an IAM policy, used to be cut down to the
     * text between its first two double quotes (kitecorp/kite#109).
     */
    public static String quote(String value) {
        if (value != null && value.length() >= 2) {
            var first = value.charAt(0);
            if ((first == '"' || first == '\'') && value.charAt(value.length() - 1) == first) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    public static List<String> extractNames(String input) {
        List<String> vars = new ArrayList<>();
        Matcher m = INTERPOLATION.matcher(input);
        while (m.find()) {
            // If group(1) is non-null, we matched ${…}, otherwise group(2) is the bare $ident
            String name = (m.group(1) != null) ? m.group(1) : m.group(2);
            vars.add(name);
        }
        return vars;
    }

    /**
     * Replaces all interpolated variables (${var} and $var) in the input
     * using the provided map.  If a variable is missing from the map, it
     * will be replaced with the empty string (change that behavior as needed).
     *
     * @param input  The template, e.g. "Hello $user, you owe ${amount} USD"
     * @param values Map from variable name → replacement value
     * @return the interpolated string
     */
    public static String replaceVariables(String input, Map<String, String> values) {
        Matcher m = INTERPOLATION.matcher(input);
        var sb = new StringBuilder();
        while (m.find()) {
            String varName = (m.group(1) != null) ? m.group(1) : m.group(2);
            // lookup replacement; default to empty-string if missing
            String replacement = values.getOrDefault(varName, "");
            // quote any literal chars in the replacement
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

}
