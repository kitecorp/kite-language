package io.zmeu.Frontend.Parser.Expressions;

import io.zmeu.Frontend.Parse.Literals.Identifier;
import io.zmeu.Frontend.Parse.Literals.PluginIdentifier;
import io.zmeu.Frontend.Parser.Statements.BlockExpression;
import io.zmeu.Frontend.Parser.Statements.Statement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ModuleExpression extends Statement {
    private PluginIdentifier type;
    @Nullable
    private Identifier name;
    private BlockExpression block;

    private ModuleExpression() {
    }

    private ModuleExpression(PluginIdentifier type, Identifier name, BlockExpression block) {
        this();
        this.type = type;
        this.name = name;
        this.block = block;
    }

    public static Statement of(PluginIdentifier type, Identifier name, BlockExpression block) {
        return new ModuleExpression(type, name, block);
    }

    public static Statement of() {
        return new ModuleExpression();
    }

    public static Statement module(String type, String name, BlockExpression operator) {
        var build = PluginIdentifier.fromString(type);
        return of(build, Identifier.id(name), operator);
    }

    public List<Statement> getArguments() {
        return block.getExpression();
    }

    public String name() {
        return name.string();
    }

}
