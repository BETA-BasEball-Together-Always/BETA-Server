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

    // 멱등성 관련 에러
    IDEMPOTENCY_KEY_DUPLICATE("IDEMPOTENCY001", "이미 처리된 요청입니다", HttpStatus.CONFLICT),
    
    // 소셜 로그인 관련 에러
    INVALID_SOCIAL_TOKEN("SOCIAL001", "유효하지 않은 소셜 로그인 토큰입니다", HttpStatus.UNAUTHORIZED),
    SOCIAL_API_ERROR("SOCIAL002", "소셜 로그인 API 호출 중 오류가 발생했습니다", HttpStatus.BAD_GATEWAY),
    
    // Validation 관련 에러
    VALIDATION_FAILED("VALIDATION001", "입력값 검증에 실패했습니다", HttpStatus.BAD_REQUEST),
    
    // 동의 관련 에러
    PERSONAL_INFO_AGREEMENT_REQUIRED("CONSENT001", "개인정보 수집 및 이용에 대한 필수 동의가 필요합니다", HttpStatus.BAD_REQUEST),
    
    // 사용자 관련 에러
    USER_NOT_FOUND("USER001", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    USER_WITHDRAWN("USER002", "탈퇴한 사용자입니다", HttpStatus.FORBIDDEN),
    USER_SUSPENDED("USER003", "정지된 사용자입니다", HttpStatus.FORBIDDEN),
    NAME_DUPLICATE("USER004", "이미 존재하는 이름입니다", HttpStatus.CONFLICT),
    // 구단 관련 에러
    TEAM_NOT_FOUND("TEAM001", "해당 구단은 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    // 게시글 관련 에러
    POST_NOT_FOUND("POST001", "게시글을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    POST_ACCESS_DENIED("POST002", "게시글에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),

    // 이미지 관련 에러
    INVALID_IMAGE_TYPE("IMAGE001", "지원하지 않는 파일 형식입니다 (jpg, jpeg, png만 가능)", HttpStatus.BAD_REQUEST),
    IMAGE_SIZE_EXCEEDED("IMAGE002", "이미지 파일 크기는 10MB를 초과할 수 없습니다", HttpStatus.BAD_REQUEST),
    IMAGE_COUNT_EXCEEDED("IMAGE003", "이미지 파일은 최대 5개까지 업로드 가능합니다", HttpStatus.BAD_REQUEST),
    IMAGE_UPLOAD_FAILED("IMAGE004", "이미지 파일 업로드 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_REQUIRED("IMAGE005", "이미지 파일이 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    IMAGE_ORDER_MISMATCH("IMAGE006", "정렬할 이미지 개수와 DB 이미지 개수가 일치하지 않습니다", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_FOUND("IMAGE007", "삭제할 이미지를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    // 서버 에러
    INTERNAL_SERVER_ERROR("SERVER001", "서버 내부 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
