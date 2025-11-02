package com.beta.infra.community.entity;

import com.beta.infra.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table( name = "hashtag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HashtagEntity extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L;

    @Builder
    public HashtagEntity(String name) {
        this.name = name;
        this.usageCount = 0L;
    }
}
