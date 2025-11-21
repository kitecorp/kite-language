package io.kite.syntax.literals;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

/**
 * A path is a sequence of one or more path segments logically separated by a package qualifier (.)
 * If a path has multiple segments it always refers to an item. If there are no multiple segments it refers to an
 * item in the local scope.
 * This usually holds a package name when importing a package or when using a schema/resource
 * PathIdentifier:
 * ; Identifier(.Identifier)*
 * ; Identifier(.Identifier)* / Type (@Date)?
 */
//@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true )
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class PluginIdentifier extends Identifier {
    private PathIdentifier path;
    private TypeIdentifier type;
    private LocalDate date;

    private PluginIdentifier() {
        super();
    }

    @Override
    public String string() {
        if (path == null) {
            return type.string();
        }
        return path.string() + "/" + type.string() + "@" + date.toString();
    }

    public static PluginIdentifier fromString(@NotNull String string) {
        string = stringBetweenQuotes(string);

        String[] split = string.split("/");
        if (split.length < 2) {
            var identifier = new PluginIdentifier();
            identifier.setType(TypeIdentifier.type(split[0]));
            return identifier;
        }
        String[] type = split[1].split("@");
        if (type.length != 2) {
            var identifier = new PluginIdentifier();
            identifier.setPath(PathIdentifier.of(split[0]));
            identifier.setType(TypeIdentifier.type(split[1]));
            return identifier;
        } else {
            var date = LocalDate.parse(type[1]);
            var identifier = new PluginIdentifier();
            identifier.setPath(PathIdentifier.of(split[0]));
            identifier.setType(TypeIdentifier.type(type[0]));
            identifier.setDate(date);
            return identifier;
        }
    }

    private static @NotNull String stringBetweenQuotes(@NotNull String string) {
        if (string.endsWith("\n") || string.endsWith("\r")) { // remove blanks from end of string so we can remove quotes after that
            string = string.substring(0, string.length() - 1);
        }
        if (string.startsWith("\"") && string.endsWith("\"") || string.startsWith("'") && string.endsWith("'")) {
            string = string.substring(1, string.length() - 1);
        }
        return string;
    }

    public static PluginIdentifier from(@NotNull TypeIdentifier type) {
        var identifier = new PluginIdentifier();
        identifier.setType(type);
        return identifier;
    }

}
