package cloud.kitelang.syntax.literals;

import cloud.kitelang.semantics.types.ReferenceType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * A path is a sequence of one or more path segments logically separated by a package qualifier (.)
 * If a path has multiple segments it always refers to an item. If there are no multiple segments it refers to an
 * item in the local scope.
 * This usually holds a package name when importing a package or when using a schema/resource
 * PathIdentifier:
 * ; .?Identifier(.Identifier)*
 */
//@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true )
@Data
@EqualsAndHashCode(callSuper = true)
public final class PathIdentifier extends Identifier {
    @Getter
    private String[] paths;

    public PathIdentifier() {
        super();
    }

    private PathIdentifier(String path) {
        this();
        paths = StringUtils.split(path, ".");
    }

    public static PathIdentifier type(String type) {
        return of(type);
    }

    public static PathIdentifier type(ReferenceType type) {
        return of(type.getValue());
    }

    public static PathIdentifier of(String object) {
        return new PathIdentifier(object);
    }

    @Override
    public String string() {
        return StringUtils.join(paths, ".");
    }

}
