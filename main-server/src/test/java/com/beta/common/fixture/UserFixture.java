package com.beta.common.fixture;

import com.beta.common.provider.SocialProvider;
import com.beta.infra.auth.entity.UserEntity;
import com.beta.infra.common.entity.BaseballTeamEntity;

/**
 * 테스트용 User 관련 엔티티 생성 유틸리티
 */
public class UserFixture {

    public static UserEntity createActiveUser(Long id, String nickName, BaseballTeamEntity team) {
        return createUser(id, nickName, team, UserEntity.UserStatus.ACTIVE, UserEntity.UserRole.USER);
    }

    public static UserEntity createActiveUser(String socialId, String nickName, BaseballTeamEntity team) {
        return UserEntity.builder()
                .socialId(socialId)
                .email(socialId + "@test.com")
                .password("$2a$10$encryptedPasswordHash")
                .nickName(nickName)
                .socialProvider(SocialProvider.KAKAO)
                .status(UserEntity.UserStatus.ACTIVE)
                .role(UserEntity.UserRole.USER)
                .baseballTeam(team)
                .gender(UserEntity.GenderType.M)
                .age(25)
                .build();
    }

    public static UserEntity createUser(Long id, String nickName, BaseballTeamEntity team,
                                       UserEntity.UserStatus status, UserEntity.UserRole role) {
        return UserEntity.builder()
                .socialId("social_" + id)
                .email("social_" + id + "@test.com")
                .password("$2a$10$encryptedPasswordHash")
                .nickName(nickName)
                .socialProvider(SocialProvider.KAKAO)
                .status(status)
                .role(role)
                .baseballTeam(team)
                .gender(UserEntity.GenderType.M)
                .age(25)
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

    public static UserEntity createNaverUser(String socialId, String nickName, BaseballTeamEntity team) {
        return UserEntity.builder()
                .socialId(socialId)
                .email(socialId + "@naver.com")
                .password("$2a$10$encryptedPasswordHash")
                .nickName(nickName)
                .socialProvider(SocialProvider.NAVER)
                .status(UserEntity.UserStatus.ACTIVE)
                .role(UserEntity.UserRole.USER)
                .baseballTeam(team)
                .gender(UserEntity.GenderType.F)
                .age(35)
                .build();
    }
}
