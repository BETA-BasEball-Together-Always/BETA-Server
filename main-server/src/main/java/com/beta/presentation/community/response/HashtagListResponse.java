package com.beta.presentation.community.response;

import com.beta.application.community.dto.HashtagDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HashtagListResponse {
    private List<HashtagItem> hashtags;

    @Getter
    @Builder
    public static class HashtagItem {
        private Long hashtagId;
        private String name;
    }

    public static HashtagListResponse from(List<HashtagDto> hashtags) {
        List<HashtagItem> items = hashtags.stream()
                .map(dto -> HashtagItem.builder()
                        .hashtagId(dto.getHashtagId())
                        .name(dto.getName())
                        .build())
                .toList();

        return HashtagListResponse.builder()
                .hashtags(items)
                .build();
    }
}
