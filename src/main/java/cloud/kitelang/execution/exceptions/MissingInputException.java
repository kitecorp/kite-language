package cloud.kitelang.execution.exceptions;

public class MissingInputException extends RuntimeException {
    public MissingInputException(String message) {
        super(message);
    }
}
