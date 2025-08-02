package io.zmeu.Frontend.Parser.Expressions;

import io.zmeu.Frontend.Parse.Literals.Identifier;
import io.zmeu.Frontend.Parse.Literals.SymbolIdentifier;
import io.zmeu.Frontend.Parse.Literals.TypeIdentifier;
import io.zmeu.Frontend.Parser.Statements.BlockExpression;
import io.zmeu.Frontend.Parser.Statements.Statement;
import io.zmeu.Runtime.Interpreter;
import io.zmeu.Runtime.Values.DeferredObserverValue;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Data
public final class ResourceExpression extends Statement implements DeferredObserverValue {
    private Identifier type;
    @Nullable
    private Identifier name;
    private BlockExpression block;
    private boolean isEvaluated;
    private boolean existing;

    private ResourceExpression() {
        this.name = new SymbolIdentifier();
    }

    public static Statement resource(String type, String name, BlockExpression operator) {
        return resource(TypeIdentifier.type(type), Identifier.id(name), operator);
    }

    public static Statement resource(boolean existing, String type, String name, BlockExpression operator) {
        return resource(existing, TypeIdentifier.type(type), Identifier.id(name), operator);
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

    private ResourceExpression(Identifier type, Identifier name, BlockExpression block) {
        this();
        this.type = type;
        this.name = name;
        this.block = block;
    }

    private ResourceExpression(boolean existing, Identifier type, Identifier name, BlockExpression block) {
        this();
        this.type = type;
        this.name = name;
        this.block = block;
        this.existing = existing;
    }

    public static Statement resource(Identifier type, Identifier name, BlockExpression block) {
        return new ResourceExpression(type, name, block);
    }

    public static Statement resource() {
        return new ResourceExpression();
    }

    public static Statement resource(TypeIdentifier type, Identifier name, BlockExpression block) {
        return new ResourceExpression(type, name, block);
    }

    public static Statement resource(boolean existing, TypeIdentifier type, Identifier name, BlockExpression block) {
        return new ResourceExpression(existing, type, name, block);
    }

    public List<Statement> getArguments() {
        return block.getExpression();
    }

    public String name() {
        return name.string();
    }

    @Override
    public Object notify(Interpreter interpreter) {
        return interpreter.visit(this);
    }

}
