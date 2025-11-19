package com.beta.infra.auth.entity;

import com.beta.common.provider.SocialProvider;
import com.beta.infra.common.entity.BaseEntity;
import com.beta.infra.common.entity.BaseballTeamEntity;
import com.nimbusds.openid.connect.sdk.claims.Gender;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Column(name = "social_id")
    private String socialId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickName", nullable = false)
    private String nickName;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false, length = 20)
    private SocialProvider socialProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "favorite_team_code", referencedColumnName = "code", nullable = false)
    private BaseballTeamEntity baseballTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 5, nullable = false)
    private GenderType gender;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Builder
    public UserEntity(String socialId, String email, String password, String nickName, SocialProvider socialProvider, UserStatus status, UserRole role, BaseballTeamEntity baseballTeam, GenderType gender, Integer age) {
        this.socialId = socialId;
        this.email = email;
        this.password = password;
        this.nickName = nickName;
        this.socialProvider = socialProvider;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.role = role != null ? role : UserRole.USER;
        this.baseballTeam = baseballTeam;
        this.gender = gender;
        this.age = age;
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
}
