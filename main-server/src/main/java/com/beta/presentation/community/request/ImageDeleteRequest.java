package com.beta.presentation.community.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDeleteRequest {

    @NotNull(message = "삭제할 이미지 ID 목록이 필요합니다")
    @NotEmpty(message = "삭제할 이미지 ID 목록이 비어있을 수 없습니다")
    @Size(min = 1, max = 5, message = "1~5개의 이미지만 삭제 가능합니다")
    private List<@NotNull(message = "이미지 ID는 null일 수 없습니다") Long> imageIds;
}
