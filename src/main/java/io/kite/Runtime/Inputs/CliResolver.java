package io.kite.Runtime.Inputs;

import io.kite.Runtime.Environment.Environment;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.Nullable;

import java.util.Scanner;

public class CliResolver extends InputResolver {
    private Ansi ansi = Ansi.ansi(50)
            .reset()
            .eraseScreen();

    public CliResolver(Environment<Object> inputs) {
        super(inputs);
    }

    @Override
    public @Nullable Object resolve(String key) {
        Object input = getInputs().get(key);
        if (input != null) {
            return input;
        }

        try (var scan = new Scanner(System.in)) {
            ansi.append("Enter value for inputs or CTRL-C to exit ").newline();
            ansi.fgMagenta()
                    .a("input ")
                    .reset()
                    .a(key)
                    .a(" = ")
                    .reset();
            System.out.println(ansi.toString());

            String value = scan.nextLine();
            return getInputs().initOrAssign(key, value);
        }
    }
}
