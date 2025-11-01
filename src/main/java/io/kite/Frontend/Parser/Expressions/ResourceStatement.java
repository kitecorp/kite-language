package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.Identifier;
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
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "resource")
@Builder(toBuilder = true)
@NoArgsConstructor
public final class ResourceStatement
        extends Statement
        implements DeferredObserverValue, Annotatable, CountAnnotatable, ProviderSupport, TagsSupport {
    private Identifier type;
    @Nullable
    private Expression name;
    private BlockExpression block;

    private boolean isEvaluated;
    private boolean isEvaluating;
    private String existing;
    private ResourceValue value;
    private Object index;
    @Singular
    private Set<AnnotationDeclaration> annotations = new HashSet<>();
    private boolean counted;
    @Singular
    private Set<Expression> dependencies; // we use expression because we can have a deferred resource
    private Set<String> providers;
    private Tags tags;
    private int unresolvedDependencyCount = 0;

    public static ResourceStatement resource(ResourceStatement expression) {
        return expression.toBuilder().build();
    }

    public static ResourceStatement resource(String existing, Identifier type, Expression name, BlockExpression block) {
        return ResourceStatement.builder()
                .existing(existing)
                .type(type)
                .name(name)
                .block(block)
                .build();
    }

    public static ResourceStatement resource() {
        return new ResourceStatement();
    }

    public static ResourceStatement resource(String type, String name, BlockExpression block) {
        return resource(TypeIdentifier.type(type), Identifier.id(name), block);
    }

    public static ResourceStatement resource(String existing, String type, String name, BlockExpression block) {
        return resource(existing, TypeIdentifier.type(type), Identifier.id(name), block);
    }

    public static ResourceStatement resource(Identifier type, Identifier name, BlockExpression block) {
        return ResourceStatement.builder().type(type).name(name).block(block).build();
    }

    public static ResourceStatement resource(Identifier type, Identifier name, Set<AnnotationDeclaration> annotations, BlockExpression block) {
        return ResourceStatement.builder().type(type).name(name).block(block).annotations(annotations).build();
    }

    // TypeIdentifier-specific convenience (if you keep TypeIdentifier separate)
    public static ResourceStatement resource(TypeIdentifier type, Identifier name, BlockExpression block) {
        return ResourceStatement.builder().type(type).name(name).block(block).build();
    }

    public static ResourceStatement resource(String existing, TypeIdentifier type, Identifier name, BlockExpression block) {
        return resource(existing, type, (Expression) name, block);
    }

    public static ResourceStatement resource(Set<AnnotationDeclaration> annotations, String existing, TypeIdentifier type, Expression name, BlockExpression block) {
        return ResourceStatement.builder().annotations(annotations).existing(existing).type(type).name(name).block(block).build();
    }

    public static ResourceStatement resource(String type, String main, BlockExpression block, AnnotationDeclaration... existing) {
        return ResourceStatement.builder().type(TypeIdentifier.type(type)).name(Identifier.id(main)).annotations(Set.of(existing)).block(block).build();
    }

    public static ResourceStatement resource(Set<AnnotationDeclaration> annotations, Identifier type, Expression name, BlockExpression body) {
        return ResourceStatement.builder().type(type).name(name).annotations(annotations).block(body).build();
    }

    public List<Statement> getArguments() {
        return block.getExpression();
    }

    @Override
    public Object notify(Interpreter interpreter) {
        return interpreter.visit(this);
    }

    /**
     * Called when a dependency resource has been resolved.
     * Uses a counter-based optimization to defer re-evaluation until ALL dependencies are satisfied.
     *
     * <p><strong>Optimization:</strong> Instead of re-evaluating on each dependency resolution,
     * this method only triggers re-evaluation when the last dependency is satisfied.
     * For a resource with N dependencies, this reduces evaluations from N+1 to 2 (initial + final).
     *
     * <p>See docs/DEPENDENCY_RESOLUTION.md for performance comparison and architecture details.
     *
     * @param interpreter          The interpreter instance
     * @param resolvedResourceName The name of the dependency that was resolved
     * @return The re-evaluated resource if all dependencies satisfied, null otherwise
     */
    @Override
    public Object notifyDependencyResolved(Interpreter interpreter, String resolvedResourceName) {
        // Decrement the unresolved dependency counter
        decrementUnresolvedDependencyCount();

        // Only perform full re-evaluation when ALL dependencies are resolved
        // This avoids multiple expensive re-evaluations
        if (hasUnresolvedDependencies()) {
            // Still waiting for other dependencies - skip re-evaluation
            return null;
        }

        // All dependencies now resolved - do full re-evaluation
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

    public void incrementUnresolvedDependencyCount() {
        unresolvedDependencyCount++;
    }

    public void decrementUnresolvedDependencyCount() {
        if (unresolvedDependencyCount > 0) {
            unresolvedDependencyCount--;
        }
    }

    public boolean hasUnresolvedDependencies() {
        return unresolvedDependencyCount > 0;
    }
}
