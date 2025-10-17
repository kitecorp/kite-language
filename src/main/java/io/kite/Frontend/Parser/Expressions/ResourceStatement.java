package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.SymbolIdentifier;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Statements.BlockExpression;
import io.kite.Frontend.Parser.Statements.Statement;
import io.kite.Frontend.annotations.Annotatable;
import io.kite.Frontend.annotations.CountAnnotatable;
import io.kite.Runtime.Decorators.ProviderSupport;
import io.kite.Runtime.Decorators.Tags;
import io.kite.Runtime.Decorators.TagsSupport;
import io.kite.Runtime.Interpreter;
import io.kite.Runtime.Values.DeferredObserverValue;
import io.kite.Runtime.Values.ResourceValue;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.ResourceType;
import io.kite.TypeChecker.Types.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
@AllArgsConstructor(staticName = "resource")
public final class ResourceStatement
        extends Statement
        implements DeferredObserverValue, Annotatable, CountAnnotatable, ProviderSupport, TagsSupport {
    private Identifier type;
    @Nullable
    private Expression name;
    private BlockExpression block;
    private boolean isEvaluated;
    private boolean isEvaluating;
    private Object existing;
    private ResourceValue value;
    private Object index;
    private Set<AnnotationDeclaration> annotations;
    private Boolean counted;
    private Set<Expression> dependencies = Set.of();
    private Set<String> providers;
    private Tags tags;

    private ResourceStatement() {
        this.name = new SymbolIdentifier();
    }

    private ResourceStatement(Identifier type, Identifier name, BlockExpression block) {
        this(false, type, name, block);
    }

    private ResourceStatement(Identifier type, Identifier name, Set<AnnotationDeclaration> annotations, BlockExpression block) {
        this(false, type, name, block);
        this.annotations = annotations;
    }

    private ResourceStatement(Object existing, Identifier type, Identifier name, BlockExpression block) {
        this(existing, type, (Expression) name, block);
    }

    private ResourceStatement(Object existing, Identifier type, Expression name, BlockExpression block) {
        this();
        this.type = type;
        this.name = name;
        this.block = block;
        this.existing = existing;
    }

    public static ResourceStatement resource(ResourceStatement expression) {
        var copy = new ResourceStatement();
        copy.type = expression.getType();
        copy.name = expression.getName();
        copy.block = expression.getBlock();
        copy.existing = expression.existing;
        copy.index = expression.index;
        copy.value = expression.value;
        copy.isEvaluated = expression.isEvaluated;
        copy.isEvaluating = expression.isEvaluating;
        copy.dependencies = expression.dependencies;
        return copy;
    }

    public static ResourceStatement resource(Object existing,
                                             Identifier type,
                                             Expression name,
                                             BlockExpression block) {
        return new ResourceStatement(existing, type, name, block);
    }

    // Convenience: no-arg
    public static Statement resource() {
        return new ResourceStatement();
    }

    // Convenience: String inputs
    public static Statement resource(String type, String name, BlockExpression block) {
        return resource(false, TypeIdentifier.type(type), Identifier.id(name), block);
    }

    public static Statement resource(boolean existing, String type, String name, BlockExpression block) {
        return resource(existing, TypeIdentifier.type(type), Identifier.id(name), block);
    }

    // Convenience: Identifier + Identifier
    public static Statement resource(Identifier type, Identifier name, BlockExpression block) {
        return resource(false, type, name, block);
    }

    // With annotations (Identifier + Identifier)
    public static Statement resource(Identifier type,
                                     Identifier name,
                                     Set<AnnotationDeclaration> annotations,
                                     BlockExpression block) {
        var res = resource(false, type, name, block);
        res.setAnnotations(annotations);
        return res;
    }

    // TypeIdentifier-specific convenience (if you keep TypeIdentifier separate)
    public static Statement resource(TypeIdentifier type, Identifier name, BlockExpression block) {
        return resource(false, type, (Expression) name, block);
    }

    public static ResourceStatement resource(boolean existing,
                                             TypeIdentifier type,
                                             Identifier name,
                                             BlockExpression block) {
        return resource(existing, type, (Expression) name, block);
    }

    // With annotations + existing + TypeIdentifier + Expression name
    public static ResourceStatement resource(Set<AnnotationDeclaration> annotations,
                                             boolean existing,
                                             TypeIdentifier type,
                                             Expression name,
                                             BlockExpression block) {
        var res = resource(existing, type, name, block);
        res.setAnnotations(annotations);
        return res;
    }

    public static ResourceStatement resource(String type, String main, BlockExpression block, AnnotationDeclaration... existing) {
        return new ResourceStatement(TypeIdentifier.type(type), Identifier.id(main), Set.of(existing), block);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceStatement that)) return false;
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
        return ResourceType.INSTANCE;
    }

    @Override
    public Boolean counted() {
        return counted != null && counted;
    }

    @Override
    public void counted(Boolean evaluatedCount) {
        this.counted = evaluatedCount;
    }

    public boolean hasIndex() {
        return index != null;
    }

    public boolean hasDependencies() {
        return dependencies != null && !dependencies.isEmpty();
    }

    public void addDependency(Expression resource) {
        dependencies().add(resource);
    }

    public Set<Expression> dependencies() {
        if (dependencies == null) {
            this.dependencies = new HashSet<>();
        }
        return dependencies;
    }

    @Override
    public Set<String> getProviders() {
        if (providers == null) {
            this.providers = new HashSet<>();
        }
        return providers;
    }

    @Override
    public void addProvider(String provider) {
        getProviders().add(provider);
    }

    @Override
    public Tags getTags() {
        if (tags == null) {
            this.tags = new Tags();
        }
        return tags;
    }

    @Override
    public void setTag(Tags tags) {
        this.tags = tags;
    }
}
