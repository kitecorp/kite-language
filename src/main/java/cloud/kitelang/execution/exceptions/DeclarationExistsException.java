package cloud.kitelang.execution.exceptions;

public class DeclarationExistsException extends RuntimeException {
    private static final String msg = "Variable is already declared: %s";

    public DeclarationExistsException(String message) {
        super(message);
    }


}
