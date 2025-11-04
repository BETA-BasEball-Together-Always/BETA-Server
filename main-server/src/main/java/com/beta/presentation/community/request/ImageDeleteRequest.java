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
public class ImageDeleteRequest {

    @NotBlank(message = "삭제할 이미지 ID 목록이 필요합니다")
    @Size(min = 1, max = 5, message = "1~5개의 이미지만 삭제 가능합니다")
    private List<Long> imageIds;
}
