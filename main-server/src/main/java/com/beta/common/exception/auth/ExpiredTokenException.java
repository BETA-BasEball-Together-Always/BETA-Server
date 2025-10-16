package com.beta.common.exception;

/**
 * 만료된 토큰에 대한 예외
 */
public class ExpiredTokenException extends RuntimeException {
    
    public ExpiredTokenException(String message) {
        super(message);
    }
    
    public ExpiredTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
