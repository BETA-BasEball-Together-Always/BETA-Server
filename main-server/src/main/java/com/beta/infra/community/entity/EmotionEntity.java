package com.beta.infra.community.entity;

import com.beta.infra.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "emotion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmotionEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "emotion_type_id", nullable = false)
    private Integer emotionTypeId;

    @Column(name = "post_id", nullable = false)
    private Long postId;
}
