package com.beta.common.exception;

/**
 * 탈퇴한 사용자에 대한 예외
 */
public class UserWithdrawnException extends RuntimeException {
    
    public UserWithdrawnException(String message) {
        super(message);
    }
    
    public UserWithdrawnException(String message, Throwable cause) {
        super(message, cause);
    }
}
