package com.beta.presentation.common.response;

import com.beta.common.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ErrorResponse {
    
    private final boolean success;
    private final String errorCode;
    private final String message;
    private final String timestamp;
    private final List<FieldError> fieldErrors;
    
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .success(false)
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
        return ErrorResponse.builder()
                .success(false)
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .fieldErrors(fieldErrors)
                .build();
    }
    
    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String message;
        private final Object rejectedValue;
        
        public static FieldError of(String field, String message, Object rejectedValue) {
            return FieldError.builder()
                    .field(field)
                    .message(message)
                    .rejectedValue(rejectedValue)
                    .build();
        }
    }
}
