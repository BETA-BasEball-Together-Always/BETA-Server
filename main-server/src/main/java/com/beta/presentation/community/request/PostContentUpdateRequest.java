package com.beta.presentation.community.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostContentUpdateRequest {

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 2000, message = "내용은 2000자 이하여야 합니다")
    private String content;

    private List<Long> deleteHashtagIds;

    @Valid
    @Size(max = 5, message = "이미지는 최대 5개까지 가능합니다")
    private List<Image> images;

    @Size(max = 5, message = "해시태그는 최대 5개까지 가능합니다")
    private List<@Size(max = 20, message = "해시태그는 20자 이하여야 합니다") String> hashtags;
}
