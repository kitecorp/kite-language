package io.kite.frontend.annotations;

import io.kite.frontend.parser.expressions.AnnotationDeclaration;
import io.kite.typechecker.types.DecoratorType;
import io.kite.typechecker.types.Type;

import java.util.Set;

public interface Annotatable {
    Set<AnnotationDeclaration> getAnnotations();

    void setAnnotations(Set<AnnotationDeclaration> anns);

    DecoratorType.Target getTarget();

    default String getTargetName() {
        return getTarget().name();
    }

    Type targetType();

    boolean hasAnnotations();

}