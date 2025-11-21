package io.kite.tool.theme;

public final class PlainTheme implements Theme {
    @Override
    public String paint(String text, Role role) {
        return text;
    }
}