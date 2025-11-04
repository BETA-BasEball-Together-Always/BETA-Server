package com.beta.presentation.community.response;

import com.beta.application.community.dto.ImageDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PostImagesResponse {
    private List<Long> uploadedImageIds;

    public static PostImagesResponse of(List<ImageDto> imageDtoList) {
        List<Long> imageIds = imageDtoList.stream()
                .map(ImageDto::getPostImageId)
                .toList();
        return PostImagesResponse.builder()
                .uploadedImageIds(imageIds)
                .build();
    }
}
