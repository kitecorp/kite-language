package cloud.kitelang.semantics;

import cloud.kitelang.syntax.ast.expressions.ComponentStatement;

import java.util.HashMap;
import java.util.Map;

public class ComponentRegistry {
    private final Map<String, ComponentStatement> declarations = new HashMap<>();

    public void registerDeclaration(String typeName, ComponentStatement declaration) {
        if (declarations.containsKey(typeName)) {
            throw new TypeError("Component type already declared: " + typeName);
        }
        if (declaration.hasName()) {
            throw new IllegalArgumentException("Cannot register a component instance as declaration");
        }
        declarations.put(typeName, declaration);
    }

    public ComponentStatement getDeclaration(String typeName) {
        return declarations.get(typeName);
    }

    public boolean hasDeclaration(String typeName) {
        return declarations.containsKey(typeName);
    }
}