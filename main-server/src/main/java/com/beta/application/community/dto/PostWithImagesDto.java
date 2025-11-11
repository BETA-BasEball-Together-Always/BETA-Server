package com.beta.application.community.dto;

import com.beta.infra.community.repository.dao.PostWithImages;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostWithImagesDto {
    private Long postId;
    private Long userId;
    private String content;
    private String channel;
    private Integer commentCount;
    private Integer emotionCount;
    private LocalDateTime createdAt;
    private List<ImageDto> images;

    public static PostWithImagesDto from(PostWithImages postWithImages) {
        return PostWithImagesDto.builder()
                .postId(postWithImages.getPostId())
                .userId(postWithImages.getUserId())
                .content(postWithImages.getContent())
                .channel(postWithImages.getChannel())
                .commentCount(postWithImages.getCommentCount())
                .emotionCount(postWithImages.getEmotionCount())
                .createdAt(postWithImages.getCreatedAt())
                .images(postWithImages.getImages().stream()
                        .map(image -> ImageDto.builder()
                                .postImageId(image.getImageId())
                                .imgUrl(image.getImgUrl())
                                .sort(image.getSort())
                                .build())
                        .toList())
                .build();
    }
}
