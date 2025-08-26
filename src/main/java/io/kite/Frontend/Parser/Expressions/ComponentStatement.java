package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.PluginIdentifier;
import io.kite.Frontend.Parser.Statements.BlockExpression;
import io.kite.Frontend.Parser.Statements.Statement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A component is a collection of resources, inputs and outputs.
 * A component can be just a declaration or a initialization (like a class/object). If the component doesn't have a name
 * then it's a declaration.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class ComponentStatement extends Statement {
    private PluginIdentifier type;
    /**
     * When missing it will be a component declaration. When present it will be an initialization
     */
    @Nullable
    private Identifier name;
    private BlockExpression block;

    private ComponentStatement() {
    }

    private ComponentStatement(PluginIdentifier type, @Nullable Identifier name, BlockExpression block) {
        this();
        this.type = type;
        this.name = name;
        this.block = block;
    }

    public static Statement component(PluginIdentifier type, Identifier name, BlockExpression block) {
        return new ComponentStatement(type, name, block);
    }

    public static Statement component() {
        return new ComponentStatement();
    }

    public static Statement component(String type, String name, BlockExpression operator) {
        var build = PluginIdentifier.fromString(type);
        return component(build, Identifier.id(name), operator);
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
     * If the name is missing then the component is a declaration. Else it's an initialization and should be initialized in interpreter
     */
    public boolean shouldInitialize() {
        return name != null;
    }

}
