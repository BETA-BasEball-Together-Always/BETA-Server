package com.beta.application.auth.mapper;

import com.beta.application.auth.dto.TeamDto;
import com.beta.application.auth.dto.UserDto;
import com.beta.domain.auth.User;
import com.beta.infra.auth.entity.UserEntity;
import com.beta.infra.common.entity.BaseballTeamEntity;
import lombok.NoArgsConstructor;

import java.util.List;

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
                .favoriteTeamCode(entity.getBaseballTeam().getCode())
                .favoriteTeamName(entity.getBaseballTeam().getTeamNameKr())
                .status(entity.getStatus().name())
                .role(entity.getRole().name())
                .build();
    }

    public static UserEntity toEntity(UserDto dto, BaseballTeamEntity baseballTeamEntity) {
        if (dto == null) {
            return null;
        }

        UserEntity.GenderType genderType = getGenderType(dto);
        UserEntity.AgeRange ageRange = getAgeRange(dto);

        return UserEntity.builder()
                .socialId(dto.getSocialId())
                .name(dto.getName())
                .socialProvider(dto.getSocialProvider())
                .baseballTeam(baseballTeamEntity)
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
                .favoriteTeamName(user.getFavoriteTeamName())
                .role(user.getRole())
                .build();
    }

    public static UserDto toDto(UserEntity user) {
        return UserDto.builder()
                .id(user.getId())
                .socialId(user.getSocialId())
                .name(user.getName())
                .socialProvider(user.getSocialProvider())
                .favoriteTeamCode(user.getBaseballTeam().getCode())
                .favoriteTeamName(user.getBaseballTeam().getTeamNameKr())
                .role(user.getRole().name())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .ageRange(user.getAgeRange() != null ? user.getAgeRange().name() : null)
                .build();
    }

    public static List<TeamDto> teamList(List<BaseballTeamEntity> baseballTeamEntityList) {
        return baseballTeamEntityList.stream()
                .map(teamEntity -> TeamDto.builder()
                        .teamCode(teamEntity.getCode())
                        .teamNameKr(teamEntity.getTeamNameKr())
                        .teamNameEn(teamEntity.getTeamNameEn())
                        .build())
                .toList();
    }

    private static UserEntity.GenderType getGenderType(UserDto dto) {
        UserEntity.GenderType genderType = null;
        if(dto.getGender() != null) {
            genderType = dto.getGender().toLowerCase().startsWith("m") ? UserEntity.GenderType.M : UserEntity.GenderType.F;
        }
        return genderType;
    }

    private static UserEntity.AgeRange getAgeRange(UserDto dto) {
        UserEntity.AgeRange ageRange = null;
        if(dto.getAgeRange() != null) {
            ageRange = switch (dto.getAgeRange()) {
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
