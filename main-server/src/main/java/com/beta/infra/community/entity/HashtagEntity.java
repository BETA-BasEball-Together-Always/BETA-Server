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

    @Column(name = "tag_name", nullable = false, unique = true, length = 50)
    private String tagName;

    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L;

    @Builder
    public HashtagEntity(String tagName) {
        this.tagName = tagName;
        this.usageCount = 0L;
    }

    // 비즈니스 메서드
    public void incrementUsageCount() {
        this.usageCount++;
    }

    public void decrementUsageCount() {
        if (this.usageCount > 0) {
            this.usageCount--;
        }
    }
}
