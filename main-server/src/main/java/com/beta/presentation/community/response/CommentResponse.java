package com.beta.presentation.community.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponse {
    private boolean success;
    private String message;

    public static CommentResponse success() {
        return CommentResponse.builder()
                .success(true)
                .message("댓글이 성공적으로 처리되었습니다.")
                .build();
    }
}
