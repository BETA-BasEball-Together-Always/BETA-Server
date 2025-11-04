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
public class ImageOrderUpdateRequest {

    @NotBlank(message = "이미지 순서 정보가 필요합니다")
    @Size(min = 1, max = 5, message = "1~5개의 이미지 순서만 변경 가능합니다")
    @Valid
    private List<ImageOrder> imageOrders;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageOrder {

        @NotBlank(message = "이미지 ID가 필요합니다")
        private Long imageId;

        @NotBlank(message = "순서 값이 필요합니다")
        @Min(value = 1, message = "순서는 1 이상이어야 합니다")
        @Max(value = 5, message = "순서는 5 이하여야 합니다")
        private Integer sort;
    }
}
