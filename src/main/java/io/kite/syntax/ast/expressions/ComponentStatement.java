package io.kite.syntax.ast.expressions;

import io.kite.execution.decorators.ProviderSupport;
import io.kite.execution.values.ComponentValue;
import io.kite.semantics.types.ComponentType;
import io.kite.semantics.types.DecoratorType;
import io.kite.semantics.types.Type;
import io.kite.syntax.annotations.Annotatable;
import io.kite.syntax.annotations.CountAnnotatable;
import io.kite.syntax.ast.statements.BlockExpression;
import io.kite.syntax.ast.statements.Statement;
import io.kite.syntax.parser.literals.Identifier;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.kite.syntax.parser.literals.Identifier.id;
import static io.kite.syntax.parser.literals.TypeIdentifier.type;

/**
 * A component is a collection of resources, inputs and outputs.
 * A component can be just a type or a initialization (like a class/object). If the component doesn't have a name
 * then it's a type.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class ComponentStatement extends Statement implements Annotatable, CountAnnotatable, ProviderSupport {
    private Identifier type;
    /**
     * When missing it will be a component type. When present it will be an initialization
     */
    @Nullable
    private Expression name;
    private ComponentValue value;
    private BlockExpression block;
    private Set<AnnotationDeclaration> annotations;
    @Singular
    private Set<Expression> dependencies;
    private Set<String> providers;
    private boolean counted;

    private ComponentStatement() {
        this.annotations = Set.of();
    }

    private ComponentStatement(Identifier type, @Nullable Identifier name, BlockExpression block, Set<AnnotationDeclaration> annotations) {
        this();
        this.type = type;
        this.name = name;
        this.block = block;
        this.annotations = annotations;
    }

    // Single private constructor - all others delegate to this
    private ComponentStatement(Identifier type, @Nullable Identifier name, BlockExpression block) {
        this(type, name, block, Set.of());
    }

    // Factory methods
    public static Statement component() {
        return new ComponentStatement();
    }

    public static Statement component(String type, BlockExpression block) {
        return new ComponentStatement(type(type), null, block);
    }

    public static Statement component(String type, String name, BlockExpression block) {
        return component(type(type), id(name), block);
    }

    public static Statement component(Identifier type, Identifier name, BlockExpression block) {
        return new ComponentStatement(type, name, block);
    }

    public static Statement component(String type, String name, BlockExpression block, AnnotationDeclaration... annotations) {
        return component(type(type), id(name), block, annotations);
    }

    public static ComponentStatement component(Identifier type, Identifier name, BlockExpression block, AnnotationDeclaration... annotations) {
        return new ComponentStatement(type, name, block, Set.of(annotations));
    }

    public static ComponentStatement component(Identifier type, Identifier name, BlockExpression block, Set<AnnotationDeclaration> annotations) {
        return new ComponentStatement(type, name, block, annotations);
    }

    public List<Statement> getArguments() {
        return block.getExpression();
    }

    public String name() {
        if (name instanceof Identifier identifier) {
            return identifier.string();
        }
        return null;
    }

    /**
     * If the name is missing then the component is a type. Else it's an initialization and should be initialized in interpreter
     */
    public boolean shouldInitialize() {
        return name != null;
    }

    @Override
    public DecoratorType.Target getTarget() {
        return DecoratorType.Target.COMPONENT;
    }

    public Type targetType() {
        return ComponentType.getInstance();
    }

    @Override
    public boolean hasAnnotations() {
        return annotations != null;
    }

    public boolean isDefinition() {
        return getType() != null && name == null;
    }

    @Override
    public Set<String> getProviders() {
        if (providers == null) {
            this.providers = new HashSet<>();
        }
        return providers;
    }

    @Override
    public void setProviders(Set<String> providers) {
        this.providers = providers;
    }

    @Override
    public void addProvider(String provider) {
        getProviders().add(provider);
    }

    public boolean hasName() {
        return getName() != null;
    }

    public boolean hasType() {
        return getType() != null;
    }
}
