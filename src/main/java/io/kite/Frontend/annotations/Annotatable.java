package io.kite.Frontend.annotations;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.Type;

import java.util.Set;

public interface Annotatable {
    Set<AnnotationDeclaration> getAnnotations();

    void setAnnotations(Set<AnnotationDeclaration> anns);

    DecoratorType.Target getTarget();

    default String getTargetName() {
        return getTarget().name();
    }

    Type targetType();

}