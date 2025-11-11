package com.beta.presentation.community.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostUploadResponse {
    private boolean success;
    private String message;

    public static PostUploadResponse success() {
        return PostUploadResponse.builder()
                .success(true)
                .message("게시물이 성공적으로 업로드되었습니다.")
                .build();
    }

    public static PostUploadResponse failure(String errorMessage) {
        return PostUploadResponse.builder()
                .success(false)
                .message("게시물 업로드에 실패했습니다: " + errorMessage)
                .build();
    }
}