package cloud.kitelang.syntax.ast.statements;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Represents an import statement.
 * Supports both:
 * - import * from "file.kite" (imports all symbols)
 * - import symbol1, symbol2 from "file.kite" (imports specific symbols)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class ImportStatement extends Statement {
    private final String filePath;
    /**
     * List of symbols to import. Empty list means import all (*).
     */
    private final List<String> symbols;

    public ImportStatement(String filePath, List<String> symbols) {
        this.filePath = filePath;
        this.symbols = symbols != null ? symbols : List.of();
    }

    /**
     * Creates an import statement that imports all symbols (*).
     */
    public static ImportStatement all(String filePath) {
        return new ImportStatement(filePath, List.of());
    }

    /**
     * Creates an import statement that imports specific symbols.
     */
    public static ImportStatement named(String filePath, List<String> symbols) {
        return new ImportStatement(filePath, symbols);
    }

    /**
     * Creates an import statement that imports a single symbol.
     */
    public static ImportStatement named(String filePath, String symbol) {
        return new ImportStatement(filePath, List.of(symbol));
    }

    /**
     * Returns true if this imports all symbols (*).
     */
    public boolean isImportAll() {
        return symbols.isEmpty();
    }

    // Keep backward compatibility
    public static ImportStatement of(String filePath) {
        return all(filePath);
    }
}