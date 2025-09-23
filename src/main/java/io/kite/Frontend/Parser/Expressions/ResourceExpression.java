package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.SymbolIdentifier;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Statements.BlockExpression;
import io.kite.Frontend.Parser.Statements.Statement;
import io.kite.Frontend.annotations.Annotatable;
import io.kite.Runtime.Interpreter;
import io.kite.Runtime.Values.DeferredObserverValue;
import io.kite.Runtime.Values.ResourceValue;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.ResourceType;
import io.kite.TypeChecker.Types.Type;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
public final class ResourceExpression extends Statement implements DeferredObserverValue, Annotatable {
    private Identifier type;
    @Nullable
    private Identifier name;
    private BlockExpression block;
    private boolean isEvaluated;
    private boolean isEvaluating;
    private boolean existing;
    private ResourceValue value;
    private Object index;
    private Set<AnnotationDeclaration> annotations;

    private ResourceExpression() {
        this.name = new SymbolIdentifier();
    }

    private ResourceExpression(Identifier type, Identifier name, BlockExpression block) {
        this();
        this.type = type;
        this.name = name;
        this.block = block;
    }

    private ResourceExpression(Identifier type, Identifier name, Set<AnnotationDeclaration> annotation, BlockExpression block) {
        this();
        this.type = type;
        this.name = name;
        this.block = block;
        this.annotations = annotation;
    }

    private ResourceExpression(boolean existing, Identifier type, Identifier name, BlockExpression block) {
        this();
        this.type = type;
        this.name = name;
        this.block = block;
        this.existing = existing;
    }

    public static Statement resource(String type, String name, BlockExpression operator) {
        return resource(TypeIdentifier.type(type), Identifier.id(name), operator);
    }

    public static ResourceExpression resource(ResourceExpression expression) {
        var copy = new ResourceExpression();
        copy.type = expression.getType();
        copy.name = expression.getName();
        copy.block = expression.getBlock();
        copy.existing = expression.existing;
        copy.index = expression.index;
        copy.value = expression.value;
        copy.isEvaluated = expression.isEvaluated;
        copy.isEvaluating = expression.isEvaluating;
        return copy;
    }

    public static Statement resource(boolean existing, String type, String name, BlockExpression operator) {
        return resource(existing, TypeIdentifier.type(type), Identifier.id(name), operator);
    }

    public static Statement resource(Identifier type, Identifier name, BlockExpression block) {
        return new ResourceExpression(type, name, block);
    }

    public static Statement resource(Identifier type, Identifier name, Set<AnnotationDeclaration> annotation, BlockExpression block) {
        return new ResourceExpression(type, name, block);
    }

    public static Statement resource() {
        return new ResourceExpression();
    }

    public static Statement resource(TypeIdentifier type, Identifier name, BlockExpression block) {
        return new ResourceExpression(type, name, block);
    }

    public static ResourceExpression resource(boolean existing, TypeIdentifier type, Identifier name, BlockExpression block) {
        return new ResourceExpression(existing, type, name, block);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceExpression that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(getType(), that.getType()) && Objects.equals(getName(), that.getName()) && Objects.equals(getBlock(), that.getBlock());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getType(), getName(), getBlock());
    }

    public List<Statement> getArguments() {
        return block.getExpression();
    }

    public String name() {
        if (index != null) {
            String formatted = "%s[%s]".formatted(name.string(), index);
//            if (value != null) {
//                value.setName(formatted);
//            }
            return formatted;
        }
//        if (value != null) {
//            value.setName(name.string());
//        }
        return name.string();
    }

    @Override
    public Object notify(Interpreter interpreter) {
        return interpreter.visit(this);
    }

    @Override
    public DecoratorType.Target getTarget() {
        return DecoratorType.Target.RESOURCE;
    }

    @Override
    public Type targetType() {
        return ResourceType.Resource;
    }
}
