package com.beta.common.exception.auth;

public class InvalidSocialTokenException extends RuntimeException {
    
    public InvalidSocialTokenException(String message) {
        super(message);
    }
    
    public InvalidSocialTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
