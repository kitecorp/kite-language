package io.kite.Runtime.Inputs;

import io.kite.Frontend.Parser.Expressions.InputDeclaration;
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
    public @Nullable Object resolve(InputDeclaration inputDeclaration) {
        Object input = getInputs().get(inputDeclaration.getId().string());
        if (input != null) {
            return input;
        }

        try (var scan = new Scanner(System.in)) {
            ansi.append("Enter value for inputs or CTRL-C to exit ").newline();
            ansi.fgMagenta()
                    .a("input ")
                    .fgBlue()
                    .a(inputDeclaration.getType().getType().getKind().name().toLowerCase())
                    .a(' ')
                    .reset()
                    .a(inputDeclaration.name())
                    .a(" = ")
                    .reset();
            System.out.println(ansi.toString());

            String value = scan.nextLine();
            return value;
        }
    }
}
