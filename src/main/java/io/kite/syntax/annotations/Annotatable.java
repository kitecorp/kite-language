package io.kite.syntax.annotations;

import io.kite.semantics.types.DecoratorType;
import io.kite.semantics.types.Type;
import io.kite.syntax.ast.expressions.AnnotationDeclaration;

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