package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.PluginIdentifier;
import io.kite.Frontend.Parser.Statements.BlockExpression;
import io.kite.Frontend.Parser.Statements.Statement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ComponentExpression extends Statement {
    private PluginIdentifier type;
    @Nullable
    private Identifier name;
    private BlockExpression block;

    private ComponentExpression() {
    }

    private ComponentExpression(PluginIdentifier type, Identifier name, BlockExpression block) {
        this();
        this.type = type;
        this.name = name;
        this.block = block;
    }

    public static Statement component(PluginIdentifier type, Identifier name, BlockExpression block) {
        return new ComponentExpression(type, name, block);
    }

    public static Statement component() {
        return new ComponentExpression();
    }

    public static Statement component(String type, String name, BlockExpression operator) {
        var build = PluginIdentifier.fromString(type);
        return component(build, Identifier.id(name), operator);
    }

    public List<Statement> getArguments() {
        return block.getExpression();
    }

    public String name() {
        return name.string();
    }

}
