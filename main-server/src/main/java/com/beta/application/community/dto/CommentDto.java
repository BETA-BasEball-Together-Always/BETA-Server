package com.beta.application.community.dto;

import com.beta.infra.community.entity.CommentEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentDto {
    private Long commentId;
    private Long postId;
    private Long userId;
    private String content;
    private Long parentId;
    private Integer depth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentDto from(CommentEntity entity) {
        return CommentDto.builder()
                .commentId(entity.getId())
                .postId(entity.getPostId())
                .userId(entity.getUserId())
                .content(entity.getContent())
                .parentId(entity.getParentId())
                .depth(entity.getDepth())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
