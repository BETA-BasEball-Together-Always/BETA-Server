package com.beta.infra.community.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "emotion_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmotionTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String name;

    public enum EmotionTypeName {
        LIKE,
        SAD,
        FUN,
        HYPE
    }
}
