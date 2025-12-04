package cloud.kitelang.execution.exceptions;

public class MissingOutputException extends RuntimeException {
    public MissingOutputException(String message) {
        super(message);
    }
}
