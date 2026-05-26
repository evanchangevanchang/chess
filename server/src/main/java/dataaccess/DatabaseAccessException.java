package dataaccess;

public class DatabaseAccessException extends RuntimeException {
    public DatabaseAccessException(String message) {
        super(message);
    }
    public DatabaseAccessException(String message, Throwable ex) {
        super(message, ex);
    }
    public DatabaseAccessException() {}
}
