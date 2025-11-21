package io.kite.tool.theme;

public interface Theme {
    String paint(String text, Role role);

    // Convenience helpers (optional)
    default String kw(String s)      { return paint(s, Role.KEYWORD); }
    default String type(String s)    { return paint(s, Role.TYPE); }
    default String identifier(String s)   { return paint(s, Role.IDENTIFIER); }
    default String string(String s)     { return paint(s, Role.STRING); }
    default String num(String s)     { return paint(s, Role.NUMBER); }
    default String bool(String s)    { return paint(s, Role.BOOLEAN); }
    default String decorator(String s)     { return paint(s, Role.DECORATOR); }
    default String punctuation(String s)   { return paint(s, Role.PUNCTUATION); }
    default String normal(String s)  { return paint(s, Role.NORMAL); }
}