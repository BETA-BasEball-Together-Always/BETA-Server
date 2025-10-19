package com.beta.presentation.auth.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogoutResponse {
    private boolean success;
    private String message;

    public static LogoutResponse success() {
        return LogoutResponse.builder()
                .success(true)
                .message("로그아웃이 완료되었습니다.")
                .build();
    }

    public static LogoutResponse failed(String errorMessage) {
        return LogoutResponse.builder()
                .success(false)
                .message(errorMessage)
                .build();
    }
}
