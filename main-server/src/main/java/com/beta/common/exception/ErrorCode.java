package com.beta.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // JWT 관련 에러
    EXPIRED_TOKEN("JWT001", "토큰이 만료되었습니다", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("JWT002", "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN_USER_INFO("JWT003", "토큰에서 사용자 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED),
    TOKEN_ERROR("JWT004", "토큰 처리 중 오류가 발생했습니다", HttpStatus.UNAUTHORIZED),
    EXPIRED_REFRESH_TOKEN("JWT005", "리프레시 토큰이 만료되었습니다", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("JWT006", "유효하지 않은 리프레시 토큰입니다", HttpStatus.UNAUTHORIZED),
    INVALID_SIGNUP_TOKEN("JWT007", "유효하지 않은 가입 토큰입니다", HttpStatus.UNAUTHORIZED),
    
    // 소셜 로그인 관련 에러
    INVALID_SOCIAL_TOKEN("SOCIAL001", "유효하지 않은 소셜 로그인 토큰입니다", HttpStatus.UNAUTHORIZED),
    SOCIAL_API_ERROR("SOCIAL002", "소셜 로그인 API 호출 중 오류가 발생했습니다", HttpStatus.BAD_GATEWAY),
    
    // Validation 관련 에러
    VALIDATION_FAILED("VALIDATION001", "입력값 검증에 실패했습니다", HttpStatus.BAD_REQUEST),
    
    // 사용자 관련 에러
    USER_NOT_FOUND("USER001", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    USER_WITHDRAWN("USER002", "탈퇴한 사용자입니다", HttpStatus.FORBIDDEN),
    USER_SUSPENDED("USER003", "정지된 사용자입니다", HttpStatus.FORBIDDEN),
    
    // 서버 에러
    INTERNAL_SERVER_ERROR("SERVER001", "서버 내부 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
