package com.beta.common.exception;

import com.beta.common.exception.auth.*;
import com.beta.common.exception.idempotency.IdempotencyKeyException;
import com.beta.common.exception.image.*;
import com.beta.common.exception.post.PostAccessDeniedException;
import com.beta.common.exception.post.PostNotFoundException;
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
     * 게시글을 찾을 수 없음
     */
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePostNotFoundException(PostNotFoundException e) {
        log.warn("Post not found: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.POST_NOT_FOUND);
        return ResponseEntity.status(ErrorCode.POST_NOT_FOUND.getStatus()).body(errorResponse);
    }

    /**
     * 게시글 접근 권한 없음
     */
    @ExceptionHandler(PostAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handlePostAccessDeniedException(PostAccessDeniedException e) {
        log.warn("Post access denied: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.POST_ACCESS_DENIED);
        return ResponseEntity.status(ErrorCode.POST_ACCESS_DENIED.getStatus()).body(errorResponse);
    }

    /**
     * 이미 처리된 요청
     */
    @ExceptionHandler(IdempotencyKeyException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyKeyException(IdempotencyKeyException e) {
        log.warn("Idempotency key exception: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.IDEMPOTENCY_KEY_DUPLICATE);
        return ResponseEntity.status(ErrorCode.IDEMPOTENCY_KEY_DUPLICATE.getStatus()).body(errorResponse);
    }

    /**
     * 이미지 필수
     */
    @ExceptionHandler(ImageRequiredException.class)
    public ResponseEntity<ErrorResponse> handleImageRequiredException(ImageRequiredException e) {
        log.warn("Image required: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.IMAGE_REQUIRED);
        return ResponseEntity.status(ErrorCode.IMAGE_REQUIRED.getStatus()).body(errorResponse);
    }

    /**
     * 이미지 형식 오류
     */
    @ExceptionHandler(InvalidImageTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidImageTypeException(InvalidImageTypeException e) {
        log.warn("Invalid image type: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_IMAGE_TYPE);
        return ResponseEntity.status(ErrorCode.INVALID_IMAGE_TYPE.getStatus()).body(errorResponse);
    }

    /**
     * 이미지 크기 초과
     */
    @ExceptionHandler(ImageSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleImageSizeExceededException(ImageSizeExceededException e) {
        log.warn("Image size exceeded: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.IMAGE_SIZE_EXCEEDED);
        return ResponseEntity.status(ErrorCode.IMAGE_SIZE_EXCEEDED.getStatus()).body(errorResponse);
    }

    /**
     * 이미지 개수 초과
     */
    @ExceptionHandler(ImageCountExceededException.class)
    public ResponseEntity<ErrorResponse> handleImageCountExceededException(ImageCountExceededException e) {
        log.warn("Image count exceeded: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.IMAGE_COUNT_EXCEEDED);
        return ResponseEntity.status(ErrorCode.IMAGE_COUNT_EXCEEDED.getStatus()).body(errorResponse);
    }

    /**
     * 이미지 업로드 실패
     */
    @ExceptionHandler(ImageUploadFailedException.class)
    public ResponseEntity<ErrorResponse> handleImageUploadFailedException(ImageUploadFailedException e) {
        log.error("Image upload failed: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.IMAGE_UPLOAD_FAILED);
        return ResponseEntity.status(ErrorCode.IMAGE_UPLOAD_FAILED.getStatus()).body(errorResponse);
    }

    /**
     * 이미지 순서 불일치
     */
    @ExceptionHandler(ImageOrderMismatchException.class)
    public ResponseEntity<ErrorResponse> handleImageOrderMismatchException(ImageOrderMismatchException e) {
        log.warn("Image order mismatch: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.IMAGE_ORDER_MISMATCH);
        return ResponseEntity.status(ErrorCode.IMAGE_ORDER_MISMATCH.getStatus()).body(errorResponse);
    }

    /**
     * 이미지를 찾을 수 없음
     */
    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleImageNotFoundException(ImageNotFoundException e) {
        log.warn("Image not found: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.IMAGE_NOT_FOUND);
        return ResponseEntity.status(ErrorCode.IMAGE_NOT_FOUND.getStatus()).body(errorResponse);
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
