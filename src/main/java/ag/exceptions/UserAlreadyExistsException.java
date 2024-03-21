package ag.exceptions;

import org.springframework.security.authentication.BadCredentialsException;

import javax.naming.AuthenticationException;

public class UserAlreadyExistsException extends BadCredentialsException {
    private final String message;
    public UserAlreadyExistsException() {
        super(null);
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
