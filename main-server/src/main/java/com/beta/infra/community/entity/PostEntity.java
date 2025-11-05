package com.beta.infra.community.entity;

import com.beta.infra.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Column(name = "emotion_count", nullable = false)
    private Integer emotionCount = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public PostEntity(Long userId, String content, Channel channel) {
        this.userId = userId;
        this.content = content;
        this.channel = channel;
    }

    public enum Channel {
        DOOSAN,
        LG,
        KIWOOM,
        KT,
        SSG,
        KIA,
        SAMSUNG,
        NC,
        HANWHA,
        LOTTE,
        ALL
    }
}
