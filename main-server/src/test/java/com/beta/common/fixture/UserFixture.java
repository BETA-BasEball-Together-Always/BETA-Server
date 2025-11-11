package com.beta.common.fixture;

import com.beta.common.provider.SocialProvider;
import com.beta.infra.auth.entity.RefreshTokenEntity;
import com.beta.infra.auth.entity.UserEntity;
import com.beta.infra.common.entity.BaseballTeamEntity;

import java.time.LocalDateTime;

/**
 * 테스트용 User 관련 엔티티 생성 유틸리티
 */
public class UserFixture {

    public static UserEntity createActiveUser(Long id, String name, BaseballTeamEntity team) {
        return createUser(id, name, team, UserEntity.UserStatus.ACTIVE, UserEntity.UserRole.USER);
    }

    public static UserEntity createActiveUser(String socialId, String name, BaseballTeamEntity team) {
        return UserEntity.builder()
                .socialId(socialId)
                .name(name)
                .socialProvider(SocialProvider.KAKAO)
                .status(UserEntity.UserStatus.ACTIVE)
                .role(UserEntity.UserRole.USER)
                .baseballTeam(team)
                .gender(UserEntity.GenderType.M)
                .ageRange(UserEntity.AgeRange.AGE_20_29)
                .build();
    }

    public static UserEntity createUser(Long id, String name, BaseballTeamEntity team,
                                       UserEntity.UserStatus status, UserEntity.UserRole role) {
        return UserEntity.builder()
                .socialId("social_" + id)
                .name(name)
                .socialProvider(SocialProvider.KAKAO)
                .status(status)
                .role(role)
                .baseballTeam(team)
                .gender(UserEntity.GenderType.M)
                .ageRange(UserEntity.AgeRange.AGE_20_29)
                .build();
    }

    public static UserEntity createSuspendedUser(Long id, BaseballTeamEntity team) {
        return createUser(id, "Suspended User", team, UserEntity.UserStatus.SUSPENDED, UserEntity.UserRole.USER);
    }

    public static UserEntity createWithdrawnUser(Long id, BaseballTeamEntity team) {
        return createUser(id, "Withdrawn User", team, UserEntity.UserStatus.WITHDRAWN, UserEntity.UserRole.USER);
    }

    public static UserEntity createAdminUser(Long id, BaseballTeamEntity team) {
        return createUser(id, "Admin User", team, UserEntity.UserStatus.ACTIVE, UserEntity.UserRole.ADMIN);
    }

    public static UserEntity createNaverUser(String socialId, String name, BaseballTeamEntity team) {
        return UserEntity.builder()
                .socialId(socialId)
                .name(name)
                .socialProvider(SocialProvider.NAVER)
                .status(UserEntity.UserStatus.ACTIVE)
                .role(UserEntity.UserRole.USER)
                .baseballTeam(team)
                .gender(UserEntity.GenderType.F)
                .ageRange(UserEntity.AgeRange.AGE_30_39)
                .build();
    }

    public static RefreshTokenEntity createRefreshToken(Long userId, String token) {
        return RefreshTokenEntity.builder()
                .userId(userId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMonths(1))
                .build();
    }

    public static RefreshTokenEntity createExpiredRefreshToken(Long userId, String token) {
        return RefreshTokenEntity.builder()
                .userId(userId)
                .token(token)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
    }
}
