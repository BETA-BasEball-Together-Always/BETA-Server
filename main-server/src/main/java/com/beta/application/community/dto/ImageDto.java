package com.beta.application.community.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageDto {
    private Long postImageId;
    private Long postId;
    private String imgUrl;
    private String originName;
    private String newName;
    private Integer order;
    private Long fileSize;
    private String mimeType;
}
