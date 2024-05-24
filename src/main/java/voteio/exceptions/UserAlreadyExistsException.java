package voteio.exceptions;

public class UserAlreadyExistsException extends Exception {
    private final String message;
    public UserAlreadyExistsException() {
        message = null;
    }
    public UserAlreadyExistsException(String message) {
        super(message);
        this.message = message;
    }
    public String toString() {
        return "UserAlreadyExistException " + message;
    }
}
