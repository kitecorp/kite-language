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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Shared utility for handling import statements in both TypeChecker and Interpreter.
 * Handles file resolution, circular import detection, parsing, and environment merging.
 * Includes a static cache to avoid re-parsing the same file multiple times.
 */
public class ImportResolver {
    /**
     * Static cache for parsed programs. Shared across all ImportResolver instances
     * so that TypeChecker and Interpreter can reuse parsed ASTs.
     * Key: normalized absolute file path, Value: parsed Program AST
     */
    private static final Map<String, Program> PARSE_CACHE = new ConcurrentHashMap<>();

    /**
     * Thread-local base path for resolving relative import paths.
     * When set, relative paths in import statements are resolved against this base.
     */
    private static final ThreadLocal<Path> BASE_PATH = new ThreadLocal<>();

    private final KiteCompiler parser;
    private final Set<String> importChain;

    public ImportResolver(KiteCompiler parser, Set<String> importChain) {
        this.parser = parser;
        this.importChain = importChain;
    }

    /**
     * Sets the base path for resolving relative import paths.
     * When set, relative paths like "providers/networking" are resolved against this base.
     * Useful for tests where the working directory differs from the resource location.
     *
     * @param basePath The base path for import resolution, or null to use current working directory
     */
    public static void setBasePath(Path basePath) {
        if (basePath == null) {
            BASE_PATH.remove();
        } else {
            BASE_PATH.set(basePath);
        }
    }

    /**
     * Gets the current base path for import resolution.
     *
     * @return The base path, or null if not set
     */
    public static Path getBasePath() {
        return BASE_PATH.get();
    }

    /**
     * Clears the parse cache. Useful for testing or when files have changed.
     */
    public static void clearCache() {
        PARSE_CACHE.clear();
    }

    /**
     * Returns the current cache size. Useful for testing and debugging.
     */
    public static int getCacheSize() {
        return PARSE_CACHE.size();
    }

    /**
     * Resolves an import statement by parsing the file(s) and delegating to the visitor.
     * Supports both file imports (path ends with .kite) and directory imports (path without .kite).
     *
     * @param <T>            The type of values in the environment
     * @param statement      The import statement to resolve
     * @param currentEnv     The current environment to merge into
     * @param visitorFactory Factory that creates the visitor environment from the parsed program
     * @throws ImportException if the import fails (file not found, circular import, IO error)
     */
    public <T> void resolve(
            ImportStatement statement,
            Environment<T> currentEnv,
            Function<Program, Environment<T>> visitorFactory
    ) {
        var importPath = statement.getFilePath();

        if (isDirectoryImport(importPath)) {
            resolveDirectoryImport(statement, currentEnv, visitorFactory);
        } else {
            resolveFileImport(statement, currentEnv, visitorFactory);
        }
    }

    /**
     * Checks if the import path refers to a directory (no .kite extension).
     */
    private boolean isDirectoryImport(String path) {
        return !path.endsWith(".kite");
    }

    /**
     * Resolves a single file import.
     */
    private <T> void resolveFileImport(
            ImportStatement statement,
            Environment<T> currentEnv,
            Function<Program, Environment<T>> visitorFactory
    ) {
        var resolvedPath = resolvePath(statement.getFilePath());
        var normalizedPath = normalizeFilePath(resolvedPath.toString());

        checkCircularImport(normalizedPath);
        validateFileExists(resolvedPath);

        importChain.add(normalizedPath);
        try {
            var program = readAndParse(resolvedPath.toString());
            var importedEnv = visitorFactory.apply(program);

            mergeEnvironment(statement, importedEnv, currentEnv);
        } finally {
            importChain.remove(normalizedPath);
        }
    }

    /**
     * Resolves a directory import by loading all .kite files and importing the relevant symbols.
     * For named imports (import X, Y from "dir"), loads all files and imports only symbols X, Y.
     * For wildcard imports (import * from "dir"), imports all symbols from all .kite files in the directory.
     */
    private <T> void resolveDirectoryImport(
            ImportStatement statement,
            Environment<T> currentEnv,
            Function<Program, Environment<T>> visitorFactory
    ) {
        var dirPath = resolvePath(statement.getFilePath());
        validateDirectoryExists(dirPath);

        // Get all .kite files in the directory
        var allFiles = getAllKiteFilesInDirectory(dirPath);

        // Build combined environment from all files
        var combinedEnv = new java.util.HashMap<String, T>();
        for (var filePath : allFiles) {
            var normalizedPath = normalizeFilePath(filePath.toString());
            checkCircularImport(normalizedPath);

            importChain.add(normalizedPath);
            try {
                var program = readAndParse(filePath.toString());
                var importedEnv = visitorFactory.apply(program);

                // Collect all symbols from this file
                combinedEnv.putAll(importedEnv.getVariables());
            } finally {
                importChain.remove(normalizedPath);
            }
        }

        // Import symbols based on whether it's wildcard or named import
        if (statement.isImportAll()) {
            // Import all symbols from combined environment
            for (var entry : combinedEnv.entrySet()) {
                currentEnv.initOrAssign(entry.getKey(), entry.getValue());
            }
        } else {
            // Import only the specified symbols
            for (var symbol : statement.getSymbols()) {
                if (!combinedEnv.containsKey(symbol)) {
                    throw new ImportException(
                            "Symbol '" + symbol + "' not found in directory '" + dirPath + "'. " +
                            "Available symbols: " + combinedEnv.keySet());
                }
                currentEnv.initOrAssign(symbol, combinedEnv.get(symbol));
            }
        }
    }

    /**
     * Gets all .kite files in a directory (non-recursive, sorted).
     */
    private List<Path> getAllKiteFilesInDirectory(Path dirPath) {
        try (Stream<Path> paths = Files.list(dirPath)) {
            return paths
                    .filter(p -> p.toString().endsWith(".kite"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new ImportException("Failed to list directory: " + dirPath, e);
        }
    }

    /**
     * Validates that the directory exists.
     */
    private void validateDirectoryExists(Path dirPath) {
        if (!Files.isDirectory(dirPath)) {
            throw new ImportException("Import directory not found: " + dirPath);
        }
    }

    /**
     * Resolves a path against the base path if set, otherwise uses as-is.
     * This allows relative paths like "providers/networking" to work when a base path is configured.
     */
    private Path resolvePath(String importPath) {
        var path = Path.of(importPath);
        var basePath = BASE_PATH.get();

        if (basePath != null && !path.isAbsolute()) {
            return basePath.resolve(path);
        }
        return path;
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
    public void validateFileExists(Path filePath) {
        if (!Files.exists(filePath)) {
            throw new ImportException("Import file not found: " + filePath);
        }
    }

    /**
     * Reads and parses a file into a Program AST.
     * Uses caching to avoid re-parsing the same file multiple times.
     */
    public Program readAndParse(String filePath) {
        var normalizedPath = normalizeFilePath(filePath);

        return PARSE_CACHE.computeIfAbsent(normalizedPath, _ -> {
            try {
                var content = Files.readString(Path.of(filePath));
                return parser.parse(content);
            } catch (IOException e) {
                throw new ImportException("Failed to read import: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Merges variables from source environment into target.
     * If the import statement specifies symbols, only those symbols are imported.
     * If it's an "import all" (*), all variables are imported.
     *
     * @param statement  The import statement (determines which symbols to import)
     * @param source     The source environment from the imported file
     * @param target     The target environment to merge into
     * @throws ImportException if a named import symbol is not found in the source
     */
    public <T> void mergeEnvironment(ImportStatement statement, Environment<T> source, Environment<T> target) {
        if (statement.isImportAll()) {
            // Import all symbols
            for (var entry : source.getVariables().entrySet()) {
                target.initOrAssign(entry.getKey(), entry.getValue());
            }
        } else {
            // Import only specified symbols
            var sourceVars = source.getVariables();
            for (var symbol : statement.getSymbols()) {
                if (!sourceVars.containsKey(symbol)) {
                    throw new ImportException(
                            "Symbol '" + symbol + "' not found in '" + statement.getFilePath() + "'. " +
                            "Available symbols: " + sourceVars.keySet());
                }
                target.initOrAssign(symbol, sourceVars.get(symbol));
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