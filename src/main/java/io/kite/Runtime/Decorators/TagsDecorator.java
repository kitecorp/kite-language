package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Runtime.Interpreter;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TagsDecorator extends DecoratorInterpreter {
    public TagsDecorator() {
        super("tags");
    }


    @Override
    public Object execute(Interpreter interpreter, AnnotationDeclaration declaration) {
        if (declaration.getTarget() instanceof TagsSupport tagsSupport) {
            if (declaration.getArgs() != null && !declaration.getArgs().getItems().isEmpty()) {
                var tags = (List<String>) interpreter.visit(declaration.getArgs());
                tagsSupport.setTag(new Tags(Set.copyOf(tags)));
            } else if (declaration.getValue() instanceof StringLiteral literal) {
                tagsSupport.setTag(Tags.tags(literal.getValue()));
            } else if (declaration.getObject() != null) {
                var tags = (Map<String, String>) interpreter.visit(declaration.getObject());
                tagsSupport.setTag(Tags.tags(tags));
            } else {
                throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
            }
        }
        return null;
    }


}
