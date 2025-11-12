package com.beta.presentation.community.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostDeleteResponse {
    private boolean success;
    private String message;

    public static PostDeleteResponse success() {
        return PostDeleteResponse.builder()
                .success(true)
                .message("게시물이 성공적으로 삭제되었습니다.")
                .build();
    }
}
