package cloud.kitelang.syntax.ast.statements;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ImportStatement extends Statement {
    private final String filePath;

    public ImportStatement(String filePath) {
        this.filePath = filePath;
    }

    public static ImportStatement of(String filePath) {
        return new ImportStatement(filePath);
    }
}