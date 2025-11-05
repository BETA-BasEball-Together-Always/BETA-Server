package com.beta.presentation.community.response;

import com.beta.application.community.dto.ImageDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ImageDeleteResponse {
    boolean deleted;
    List<ImageDto> images;

    public static ImageDeleteResponse success(List<ImageDto> images) {
        return ImageDeleteResponse.builder()
                .deleted(true)
                .images(images)
                .build();
    }

    public static ImageDeleteResponse fail(List<ImageDto> images) {
        return ImageDeleteResponse.builder()
                .deleted(false)
                .images(images)
                .build();
    }
}
