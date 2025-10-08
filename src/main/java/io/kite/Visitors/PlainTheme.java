package io.kite.Visitors;

public final class PlainTheme implements Theme {
    @Override
    public String paint(String text, Role role) {
        return text;
    }
}