package shi.quan.common.exception;

public class BuzzException extends Exception {
    public BuzzException() {
    }

    public BuzzException(String message) {
        super(message);
    }

    public BuzzException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuzzException(Throwable cause) {
        super(cause);
    }
}
