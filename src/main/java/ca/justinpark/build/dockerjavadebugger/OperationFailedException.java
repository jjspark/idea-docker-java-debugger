package ca.justinpark.build.dockerjavadebugger;

import java.io.Serial;

public class OperationFailedException extends Exception {
    @Serial
    private static final long serialVersionUID = 7759149753473859402L;

    public OperationFailedException(String message) {
        super(message);
    }

    public OperationFailedException(String message, Exception cause) {
        super(message, cause);
    }
}
