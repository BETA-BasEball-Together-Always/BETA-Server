package com.beta.presentation.community.response;

import com.beta.application.community.dto.ImageDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PostImagesResponse {
    private List<ImageDto> uploadedImages;

    public static PostImagesResponse from(List<ImageDto> imageDtoList) {
        return PostImagesResponse.builder()
                .uploadedImages(imageDtoList)
                .build();
    }
}
