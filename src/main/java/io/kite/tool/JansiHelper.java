package io.kite.tool;

public class JansiHelper {
    public static String strip(String input) {
        // Matches any ANSI escape code: ESC [ ... letters like m, J, K, etc.
        return input.replaceAll("\\u001B\\[[;\\d]*[ -/]*[@-~]", "").trim();
    }
}
