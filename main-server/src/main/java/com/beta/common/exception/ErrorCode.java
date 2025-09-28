package com.beta.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ===== JWT 관련 에러 =====
    EXPIRED_TOKEN("JWT001", "토큰이 만료되었습니다", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("JWT002", "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN_USER_INFO("JWT003", "토큰에서 사용자 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED),
    TOKEN_ERROR("JWT004", "토큰 처리 중 오류가 발생했습니다", HttpStatus.UNAUTHORIZED),
    
    // ===== Validation 관련 에러 =====
    VALIDATION_FAILED("VALIDATION001", "입력값 검증에 실패했습니다", HttpStatus.BAD_REQUEST),
    
    // ===== 서버 에러 =====
    INTERNAL_SERVER_ERROR("SERVER001", "서버 내부 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
