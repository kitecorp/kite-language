package io.kite.Runtime.Inputs;

import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import org.jetbrains.annotations.Nullable;

import java.util.Scanner;

public class CliResolver extends InputResolver {

    public CliResolver() {
        super();
    }

    @Override
    @Nullable String resolve(InputDeclaration inputDeclaration, String previousValue) {
        if (previousValue != null) {
            // if previous value is not null we don't need to as the user for input.
            // in other words if the input was resolved, skip asking for input.
            return null;
        }

        try (var scan = new Scanner(System.in)) {
            ansi.append("Enter value for inputs or CTRL-C to exit ").newline();
            ansi.fgMagenta()
                    .a("input ")
                    .fgBlue()
                    .a(inputDeclaration.getType().getType().getValue().toLowerCase())
                    .a(' ')
                    .reset()
                    .a(inputDeclaration.name())
                    .a(" = ")
                    .reset();
            System.out.println(ansi.toString());

            var value = scan.nextLine();
            return value;
        }
    }
}
