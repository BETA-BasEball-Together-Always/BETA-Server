package com.beta.infra.community.repository.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostWithImages {
    private Long postId;
    private Long userId;
    private String content;
    private String channel;
    private Integer commentCount;
    private Integer likeCount;
    private Integer sadCount;
    private Integer funCount;
    private Integer hypeCount;
    private LocalDateTime createdAt;
    private List<Images> images;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Images {
        private Long imageId;
        private String imgUrl;
        private Integer sort;
    }
}
