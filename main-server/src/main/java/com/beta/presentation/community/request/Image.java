package com.beta.presentation.community.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Image {
    @NotNull(message = "이미지 아이디는 필수입니다")
    private Long imageId;

    @NotNull(message = "정렬 순서는 필수입니다")
    @Min(value = 1, message = "정렬은 1 이상이어야 합니다")
    @Max(value = 5, message = "정렬은 5 이하이어야 합니다")
    private Integer sort;
}
