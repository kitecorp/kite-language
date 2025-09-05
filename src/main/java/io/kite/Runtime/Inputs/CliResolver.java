package io.kite.Runtime.Inputs;

import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import org.jetbrains.annotations.Nullable;

import java.util.Scanner;

public class CliResolver extends InputResolver {

    public CliResolver() {
        super();
    }

    @Override
    @Nullable String resolve(InputDeclaration inputDeclaration) {
//        String input = getInputs().get(inputDeclaration.getId().string());
//        if (input != null) {
//            return input;
//        }

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
