package com.beta.application.community.dto;

import com.beta.infra.community.entity.PostImageEntity;
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
    private Integer sort;
    private Long fileSize;
    private String mimeType;

    public static ImageDto toDto(PostImageEntity entity) {
        return ImageDto.builder()
                .postImageId(entity.getId())
                .postId(entity.getPostId())
                .imgUrl(entity.getImgUrl())
                .originName(entity.getOriginName())
                .newName(entity.getNewName())
                .fileSize(entity.getFileSize())
                .mimeType(entity.getMimeType())
                .sort(entity.getSort())
                .build();
    }
}
