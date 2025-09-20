package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.PluginIdentifier;
import io.kite.Frontend.Parser.Statements.BlockExpression;
import io.kite.Frontend.Parser.Statements.Statement;
import io.kite.Frontend.annotations.Annotatable;
import io.kite.TypeChecker.Types.DecoratorType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * A component is a collection of resources, inputs and outputs.
 * A component can be just a type or a initialization (like a class/object). If the component doesn't have a name
 * then it's a type.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class ComponentStatement extends Statement implements Annotatable {
    private PluginIdentifier type;
    /**
     * When missing it will be a component type. When present it will be an initialization
     */
    @Nullable
    private Identifier name;
    private BlockExpression block;
    private Set<AnnotationDeclaration> annotations;

    private ComponentStatement() {
        this.annotations = Set.of();
    }

    private ComponentStatement(PluginIdentifier type, @Nullable Identifier name, BlockExpression block) {
        this();
        this.type = type;
        this.name = name;
        this.block = block;
    }

    private ComponentStatement(PluginIdentifier type, @Nullable Identifier name, BlockExpression block, Set<AnnotationDeclaration> annotations) {
        this();
        this.type = type;
        this.name = name;
        this.block = block;
        this.annotations = annotations;
    }

    public static Statement component(PluginIdentifier type, Identifier name, BlockExpression block, AnnotationDeclaration... annotations) {
        return new ComponentStatement(type, name, block, Set.of(annotations));
    }

    public static ComponentStatement component(PluginIdentifier type, Identifier name, BlockExpression block, Set<AnnotationDeclaration> annotations) {
        return new ComponentStatement(type, name, block, annotations);
    }

    public static Statement component() {
        return new ComponentStatement();
    }

    public static Statement component(String type, String name, BlockExpression operator) {
        var build = PluginIdentifier.fromString(type);
        return component(build, Identifier.id(name), operator);
    }

    public static Statement component(String type, String name, BlockExpression operator, AnnotationDeclaration... annotations) {
        var build = PluginIdentifier.fromString(type);
        return component(build, Identifier.id(name), operator, annotations);
    }

    public static Statement component(String type, BlockExpression operator) {
        var build = PluginIdentifier.fromString(type);
        return new ComponentStatement(build, null, operator);
    }

    public List<Statement> getArguments() {
        return block.getExpression();
    }

    public String name() {
        return name.string();
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
}
