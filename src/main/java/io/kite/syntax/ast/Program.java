package io.kite.syntax.ast;

import io.kite.syntax.ast.statements.Statement;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public final class Program extends Statement {
    private List<Statement> body;

    public Program() {
        this(new ArrayList<>());
    }

    public Program(List<Statement> body) {
        this.body = body;
    }

    public static Program of(Statement... body) {
        return new Program(Arrays.stream(body).toList());
    }

    public static Program program(Statement... body) {
        return new Program(Arrays.stream(body).toList());
    }

    public void addStatement(Statement statement) {
        this.body.add(statement);
    }

    public void add(Statement statement) {
        this.addStatement(statement);
    }

    public Statement first() {
        return body.listIterator().next();
    }


    public boolean hasBody() {
        return body != null;
    }
}