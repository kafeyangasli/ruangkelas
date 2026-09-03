package exceptions;

public class PersistenceException extends AppException {
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenceException(String message) {
        super(message);
    }

    public enum Type {
        SAVE_FAIL,
        LOAD_FAIL
    }
}
