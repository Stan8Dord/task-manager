package customexceptions;

public class WrongIdException extends RuntimeException {
    public WrongIdException() {
        super();
    }

    public WrongIdException(final String message) {
        super(message);
    }
}
