package com.beta.presentation.community.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 2000, message = "내용은 2000자 이하여야 합니다")
    private String content;

    @NotBlank(message = "채널은 필수입니다")
    private String channel;

    @Size(max = 10, message = "해시태그는 최대 10개까지 가능합니다")
    private List<String> hashtags;
}
