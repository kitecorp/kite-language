package cloud.kitelang.analysis;

import cloud.kitelang.execution.environment.Environment;
import cloud.kitelang.syntax.ast.KiteCompiler;
import cloud.kitelang.syntax.ast.Program;
import cloud.kitelang.syntax.ast.statements.ImportStatement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Function;

/**
 * Shared utility for handling import statements in both TypeChecker and Interpreter.
 * Handles file resolution, circular import detection, parsing, and environment merging.
 */
public class ImportResolver {
    private final KiteCompiler parser;
    private final Set<String> importChain;

    public ImportResolver(Set<String> importChain) {
        this.parser = new KiteCompiler();
        this.importChain = importChain;
    }

    public ImportResolver(KiteCompiler parser, Set<String> importChain) {
        this.parser = parser;
        this.importChain = importChain;
    }

    /**
     * Resolves an import statement by parsing the file and delegating to the visitor.
     *
     * @param statement      The import statement to resolve
     * @param currentEnv     The current environment to merge into
     * @param stdlibNames    Names to exclude from merging (built-in functions/types)
     * @param visitorFactory Factory that creates the visitor environment from the parsed program
     * @param <T>            The type of values in the environment
     * @throws ImportException if the import fails (file not found, circular import, IO error)
     */
    public <T> void resolve(
            ImportStatement statement,
            Environment<T> currentEnv,
            Set<String> stdlibNames,
            Function<Program, Environment<T>> visitorFactory
    ) {
        var filePath = normalizeFilePath(statement.getFilePath());

        checkCircularImport(filePath);
        validateFileExists(statement.getFilePath());

        importChain.add(filePath);
        try {
            var program = readAndParse(statement.getFilePath());
            var importedEnv = visitorFactory.apply(program);

            mergeEnvironment(importedEnv, currentEnv, stdlibNames);
        } finally {
            importChain.remove(filePath);
        }
    }

    /**
     * Normalizes a file path to absolute form for consistent cycle detection.
     */
    public String normalizeFilePath(String path) {
        return Paths.get(path).toAbsolutePath().normalize().toString();
    }

    /**
     * Checks for circular imports and throws if detected.
     * Includes the full import chain in the error message for debugging.
     */
    public void checkCircularImport(String normalizedPath) {
        if (importChain.contains(normalizedPath)) {
            var chain = new ArrayList<>(importChain);
            chain.add(normalizedPath);
            throw new ImportException("Circular import detected: " + String.join(" -> ", chain));
        }
    }

    /**
     * Validates that the import file exists.
     */
    public void validateFileExists(String filePath) {
        if (!Files.exists(Path.of(filePath))) {
            throw new ImportException("Import file not found: " + filePath);
        }
    }

    /**
     * Reads and parses a file into a Program AST.
     */
    public Program readAndParse(String filePath) {
        try {
            var content = Files.readString(Path.of(filePath));
            return parser.parse(content);
        } catch (IOException e) {
            throw new ImportException("Failed to read import: " + e.getMessage(), e);
        }
    }

    /**
     * Merges variables from source environment into target, excluding specified names.
     */
    public <T> void mergeEnvironment(Environment<T> source, Environment<T> target, Set<String> excludeNames) {
        for (var entry : source.getVariables().entrySet()) {
            if (!excludeNames.contains(entry.getKey())) {
                target.initOrAssign(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Exception thrown when import resolution fails.
     */
    public static class ImportException extends RuntimeException {
        public ImportException(String message) {
            super(message);
        }

        public ImportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}