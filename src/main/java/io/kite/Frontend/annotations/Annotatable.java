package io.kite.Frontend.annotations;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;

import java.util.Set;

public interface Annotatable {
    Set<AnnotationDeclaration> getAnnotations();

    void setAnnotations(Set<AnnotationDeclaration> anns);
}