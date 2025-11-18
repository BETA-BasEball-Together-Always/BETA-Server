package com.beta.presentation.community.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmotionResponse {
    private boolean success;
    private String message;

    public static EmotionResponse success() {
        return EmotionResponse.builder()
                .success(true)
                .message("게시물에 대한 반응이 성공적으로 업데이트되었습니다.")
                .build();
    }
}
