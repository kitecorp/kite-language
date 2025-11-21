package io.kite.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

public class AstGenerator {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            var cwd = Paths.get(".").normalize().toAbsolutePath() + "/src/main/java/io/kite/Frontend/Parser";
            System.out.println(cwd);
            args = new String[]{cwd};
        }
        String outputDir = args[0];

//        defineAst(outputDir + "/Expressions", "dev.fangscl.frontend.parser.Expressions", "Expr", Arrays.asList(
//                "Binary   : Expr left, Token operator, Expr right",
//                "Grouping : Expr expression",
//                "Literal  : Object value",
//                "Unary    : Token operator, Expr right"
//        ));

    }

    private static void defineAst(String outputDir, String packageName, String baseName, List<String> types)
            throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        try (var writer = new PrintWriter(path, StandardCharsets.UTF_8)) {
            writer.println("""
                    package %s;
                            
                    /**
                     * Expression
                     * : AssignmentExpression
                     * | BlockStatement
                     * ;
                     */
                    public abstract class %s {
                        
                    }
                    """.formatted(packageName, baseName));
        }
        for (String type : types) {
            String className = type.split(":")[0].trim();
            className = className + baseName;
            try (var writer = new PrintWriter("%s/%s.java".formatted(outputDir, className), StandardCharsets.UTF_8)) {
                String fields = type.split(":")[1].trim();
                defineType(writer, packageName, baseName, className, fields);
            }
        }
    }

    private static void defineType(PrintWriter writer, String packageName, String superclass, String className, String fieldList) {
        var constructorInit = new StringBuilder();
        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            constructorInit.append("this.%s = %s;%n".formatted(name, name));
        }
        var constructor = """
                %s(%s){
                    %s
                }
                """.formatted(className, fieldList, constructorInit.toString());
        var imports = "";
        if (fieldList.contains("Token")) {
            imports = "import io.kite.frontend.lexer.Token;";
        }

        // Fields.
        var classFields = "";
        for (String field : fields) {
            classFields += "private final " + field + ";";
        }

        writer.println("""
                package %s;
                                
                import lombok.Data;
                import lombok.EqualsAndHashCode;
                %s
                                
                @Data
                @EqualsAndHashCode(callSuper = true)
                public class %s extends %s {
                    %s
                                
                    %s
                    
                }
                """.formatted(packageName, imports, className, superclass, classFields, constructor));

    }
}
