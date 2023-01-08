package ca.justinpark.build.dockerjavadebugger;

public class OperationFailedException extends Exception {
    private static final long serialVersionUID = 7759149753473859402L;

    public OperationFailedException(String message) {
        super(message);
    }

    public OperationFailedException(String message, Exception cause) {
        super(message, cause);
    }
}
