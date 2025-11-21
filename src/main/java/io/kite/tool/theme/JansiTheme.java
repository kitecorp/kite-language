package io.kite.tool.theme;

import org.fusesource.jansi.Ansi;

public final class JansiTheme implements Theme {
    @Override
    public String paint(String text, Role role) {
        var ansi = Ansi.ansi();
        switch (role) {
            case KEYWORD -> ansi.fgMagenta();
            case TYPE -> ansi.fgBlue();
            case IDENTIFIER -> ansi.fgDefault();    // identifiers often plain; tweak if you like
            case STRING -> ansi.fgGreen();
            case NUMBER -> ansi.fgCyan();
            case BOOLEAN -> ansi.fgCyan();
            case DECORATOR -> ansi.fgYellow();
            case PUNCTUATION -> ansi.fgDefault();
            case NORMAL -> ansi.fgDefault();
        }
        return ansi.a(text).fgDefault().toString();
    }
}