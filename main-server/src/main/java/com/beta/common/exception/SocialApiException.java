package com.beta.common.exception;

public class SocialApiException extends RuntimeException {
    
    public SocialApiException(String message) {
        super(message);
    }
    
    public SocialApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
