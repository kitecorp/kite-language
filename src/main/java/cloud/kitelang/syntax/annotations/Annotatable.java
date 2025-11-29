package cloud.kitelang.syntax.annotations;

import cloud.kitelang.semantics.types.DecoratorType;
import cloud.kitelang.semantics.types.Type;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;

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