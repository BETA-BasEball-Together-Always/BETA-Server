package com.beta.infra.community.entity;

import com.beta.infra.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "emotion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmotionEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "emotion_type", nullable = false)
    private EmotionType emotionType;

    @Builder
    public EmotionEntity(Long userId, Long postId, EmotionType emotionType) {
        this.userId = userId;
        this.postId = postId;
        this.emotionType = emotionType;
    }

    public void changeEmotionType(EmotionType emotionType) {
        this.emotionType = emotionType;
    }

    @Getter
    public enum EmotionType {
        LIKE,
        SAD,
        FUN,
        HYPE
    }
}
