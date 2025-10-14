package com.beta.application.auth.mapper;

import com.beta.application.auth.dto.UserDto;
import com.beta.common.exception.UserSuspendedException;
import com.beta.common.exception.UserWithdrawnException;
import com.beta.domain.auth.User;
import com.beta.infra.auth.entity.UserEntity;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class UserMapper {

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.builder()
                .id(entity.getId())
                .socialId(entity.getSocialId())
                .name(entity.getName())
                .socialProvider(entity.getSocialProvider())
                .favoriteTeamCode(entity.getFavoriteTeamCode())
                .status(entity.getStatus().name())
                .role(entity.getRole().name())
                .build();
    }

    public static UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }

        UserEntity.GenderType genderType = getGenderType(domain);
        UserEntity.AgeRange ageRange = getAgeRange(domain);

        return UserEntity.builder()
                .socialId(domain.getSocialId())
                .name(domain.getName())
                .socialProvider(domain.getSocialProvider())
                .favoriteTeamCode(domain.getFavoriteTeamCode())
                .gender(genderType)
                .ageRange(ageRange)
                .build();
    }

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .socialId(user.getSocialId())
                .name(user.getName())
                .socialProvider(user.getSocialProvider())
                .favoriteTeamCode(user.getFavoriteTeamCode())
                .build();
    }

    private static UserEntity.GenderType getGenderType(User domain) {
        UserEntity.GenderType genderType = null;
        if(domain.getGender() != null) {
            genderType = domain.getGender().toLowerCase().startsWith("m") ? UserEntity.GenderType.M : UserEntity.GenderType.F;
        }
        return genderType;
    }

    private static UserEntity.AgeRange getAgeRange(User domain) {
        UserEntity.AgeRange ageRange = null;
        if(domain.getAgeRange() != null) {
            ageRange = switch (domain.getAgeRange()) {
                case "0-9" -> UserEntity.AgeRange.AGE_0_9;
                case "10-19" -> UserEntity.AgeRange.AGE_10_19;
                case "20-29" -> UserEntity.AgeRange.AGE_20_29;
                case "30-39" -> UserEntity.AgeRange.AGE_30_39;
                case "40-49" -> UserEntity.AgeRange.AGE_40_49;
                case "50-59" -> UserEntity.AgeRange.AGE_50_59;
                default -> UserEntity.AgeRange.AGE_60_ABOVE;
            };
        }
        return ageRange;
    }
}
