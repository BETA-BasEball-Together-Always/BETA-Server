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
public class PostCreateRequest {

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 2000, message = "내용은 2000자 이하여야 합니다")
    private String content;

    private Boolean allChannel;

    @Valid
    @Size(max = 5, message = "이미지는 최대 5개까지 가능합니다")
    private List<Image> images;

    @Size(max = 10, message = "해시태그는 최대 10개까지 가능합니다")
    private List<Long> hashtags;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
        @NotNull(message = "이미지 아이디는 필수입니다")
        private Long imageId;

        @NotNull(message = "정렬 순서는 필수입니다")
        @Min(value = 1, message = "정렬은 1 이상이어야 합니다")
        @Max(value = 5, message = "정렬은 5 이하이어야 합니다")
        private Integer sort;
    }
}
