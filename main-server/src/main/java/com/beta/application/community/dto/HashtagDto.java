package com.beta.application.community.dto;

import com.beta.infra.community.entity.HashtagEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HashtagDto {
    private Long hashtagId;
    private String name;

    public static HashtagDto from(HashtagEntity entity) {
        return HashtagDto.builder()
                .hashtagId(entity.getId())
                .name(entity.getName())
                .build();
    }
}
