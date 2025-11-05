package com.beta.presentation.community.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageOrderUpdateRequest {

    @NotEmpty(message = "이미지 순서 정보가 필요합니다")
    @Size(min = 1, max = 5, message = "1~5개의 이미지 순서만 변경 가능합니다")
    private List<Long> imageOrders;
}
