package com.beta.infra.auth.entity;

import com.beta.common.provider.SocialProvider;
import com.beta.infra.common.entity.BaseEntity;
import com.nimbusds.openid.connect.sdk.claims.Gender;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Column(name = "social_id", nullable = false)
    private String socialId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false, length = 20)
    private SocialProvider socialProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    @Column(name = "favorite_team_code", length = 10)
    private String favoriteTeamCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 5)
    private GenderType gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_range", length = 20)
    private AgeRange ageRange;

    @Builder
    public UserEntity(String socialId, String name, SocialProvider socialProvider, UserStatus status, UserRole role, String favoriteTeamCode, GenderType gender, AgeRange ageRange) {
        this.socialId = socialId;
        this.name = name;
        this.socialProvider = socialProvider;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.role = role != null ? role : UserRole.USER;
        this.favoriteTeamCode = favoriteTeamCode;
        this.gender = gender;
        this.ageRange = ageRange;
    }

    public enum UserStatus {
        ACTIVE,     // 정상 사용
        SUSPENDED,  // 정지
        WITHDRAWN   // 탈퇴
    }

    public enum UserRole {
        USER, ADMIN
    }

    public enum GenderType {
        M, F;
    }

    public enum AgeRange {
        AGE_0_9, AGE_10_19, AGE_20_29, AGE_30_39, AGE_40_49, AGE_50_59, AGE_60_ABOVE
    }
}
