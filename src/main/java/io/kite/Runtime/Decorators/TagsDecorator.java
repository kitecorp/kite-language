package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Runtime.Interpreter;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TagsDecorator extends DecoratorInterpreter {
    private final Interpreter interpreter;

    public TagsDecorator(Interpreter interpreter) {
        super("tags");
        this.interpreter = interpreter;
    }


    @Override
    public Object execute(AnnotationDeclaration declaration) {
        if (declaration.getTarget() instanceof TagsSupport tagsSupport) {
            if (declaration.getArgs() != null && !declaration.getArgs().getItems().isEmpty()) {
                var tags = (List<String>) interpreter.visit(declaration.getArgs());
                tagsSupport.setTags(new Tags(Set.copyOf(tags)));
            } else if (declaration.getValue() instanceof StringLiteral literal) {
                tagsSupport.setTags(Tags.tags(literal.getValue()));
            } else if (declaration.getObject() != null) {
                var tags = (Map<String, String>) interpreter.visit(declaration.getObject());
                tagsSupport.setTags(Tags.tags(tags));
            } else {
                throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
            }
        }
        return null;
    }


}
