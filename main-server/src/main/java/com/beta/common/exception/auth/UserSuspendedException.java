package com.beta.common.exception;

/**
 * 정지된 사용자에 대한 예외
 */
public class UserSuspendedException extends RuntimeException {
    
    public UserSuspendedException(String message) {
        super(message);
    }
    
    public UserSuspendedException(String message, Throwable cause) {
        super(message, cause);
    }
}
