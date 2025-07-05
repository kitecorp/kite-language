package io.zmeu.Frontend.Parser.Literals;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class LiteralUtils {
    public static String quote(String value) {
        return Optional.ofNullable(StringUtils.substringBetween(value, "\"", "\""))
                .or(() -> Optional.ofNullable(StringUtils.substringBetween(value, "'", "'")))
                .orElse(value);
    }
}
