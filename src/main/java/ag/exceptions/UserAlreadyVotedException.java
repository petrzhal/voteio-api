package ag.exceptions;

public class UserAlreadyVotedException extends UserAlreadyExistsException {
    private final String message;
    public UserAlreadyVotedException() {
        message = null;
    }
    public UserAlreadyVotedException(String message) {
        super(message);
        this.message = message;
    }
    public String toString() {
        return "UserAlreadyVotedException " + message;
    }
}
