package com.beta.presentation.community.response;

import com.beta.application.community.dto.ImageDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostImagesResponse {
    private Long imageId;
    private String imageUrl;

    public static PostImagesResponse from(ImageDto dto) {
        return PostImagesResponse.builder()
                .imageId(dto.getPostImageId())
                .imageUrl(dto.getImgUrl())
                .build();
    }
}
