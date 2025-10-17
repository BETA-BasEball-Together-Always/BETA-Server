package com.beta.common.exception;

import com.beta.common.exception.auth.*;
import com.beta.common.exception.team.TeamNotFoundException;
import com.beta.presentation.common.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(NameDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleNameDuplicateException(NameDuplicateException e) {
        log.warn("Name duplicate exception: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.NAME_DUPLICATE);
        return ResponseEntity.status(ErrorCode.NAME_DUPLICATE.getStatus()).body(errorResponse);
    }

    /**
     * @Valid 어노테이션으로 validation이 실패한 경우
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());
        
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> ErrorResponse.FieldError.of(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue()
                ))
                .toList();
        
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.VALIDATION_FAILED, fieldErrors);
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getStatus()).body(errorResponse);
    }

    /**
     * 개인정보 동의 필수 항목 누락
     */
    @ExceptionHandler(PersonalInfoAgreementRequiredException.class)
    public ResponseEntity<ErrorResponse> handlePersonalInfoAgreementRequiredException(PersonalInfoAgreementRequiredException e) {
        log.warn("Personal info agreement required: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.PERSONAL_INFO_AGREEMENT_REQUIRED);
        return ResponseEntity.status(ErrorCode.PERSONAL_INFO_AGREEMENT_REQUIRED.getStatus()).body(errorResponse);
    }

    /**
     * 소셜 로그인 토큰 무효
     */
    @ExceptionHandler(InvalidSocialTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSocialTokenException(InvalidSocialTokenException e) {
        log.warn("Invalid social token: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_SOCIAL_TOKEN);
        return ResponseEntity.status(ErrorCode.INVALID_SOCIAL_TOKEN.getStatus()).body(errorResponse);
    }

    /**
     * 소셜 API 호출 오류
     */
    @ExceptionHandler(SocialApiException.class)
    public ResponseEntity<ErrorResponse> handleSocialApiException(SocialApiException e) {
        log.error("Social API error: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.SOCIAL_API_ERROR);
        return ResponseEntity.status(ErrorCode.SOCIAL_API_ERROR.getStatus()).body(errorResponse);
    }

    /**
     * 유효하지 않은 토큰
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException e) {
        log.warn("Invalid token: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_TOKEN);
        return ResponseEntity.status(ErrorCode.INVALID_TOKEN.getStatus()).body(errorResponse);
    }

    /**
     * 만료된 토큰
     */
    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<ErrorResponse> handleExpiredTokenException(ExpiredTokenException e) {
        log.warn("Expired token: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.EXPIRED_TOKEN);
        return ResponseEntity.status(ErrorCode.EXPIRED_TOKEN.getStatus()).body(errorResponse);
    }

    /**
     * 사용자를 찾을 수 없음
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("User not found: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.USER_NOT_FOUND);
        return ResponseEntity.status(ErrorCode.USER_NOT_FOUND.getStatus()).body(errorResponse);
    }

    /**
     * 구단을 찾을 수 없음
     */
    @ExceptionHandler(TeamNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTeamNotFoundException(TeamNotFoundException e) {
        log.warn("Team not found: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.TEAM_NOT_FOUND);
        return ResponseEntity.status(ErrorCode.TEAM_NOT_FOUND.getStatus()).body(errorResponse);
    }

    /**
     * 탈퇴한 사용자
     */
    @ExceptionHandler(UserWithdrawnException.class)
    public ResponseEntity<ErrorResponse> handleUserWithdrawnException(UserWithdrawnException e) {
        log.warn("Withdrawn user tried to access: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.USER_WITHDRAWN);
        return ResponseEntity.status(ErrorCode.USER_WITHDRAWN.getStatus()).body(errorResponse);
    }

    /**
     * 정지된 사용자
     */
    @ExceptionHandler(UserSuspendedException.class)
    public ResponseEntity<ErrorResponse> handleUserSuspendedException(UserSuspendedException e) {
        log.warn("Suspended user tried to access: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.USER_SUSPENDED);
        return ResponseEntity.status(ErrorCode.USER_SUSPENDED.getStatus()).body(errorResponse);
    }

    /**
     * Exception 처리 (최상위 예외 - 모든 예외 처리)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(errorResponse);
    }
}
