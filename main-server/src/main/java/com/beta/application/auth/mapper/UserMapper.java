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
                .email(entity.getEmail())
                .password(entity.getPassword())
                .socialId(entity.getSocialId())
                .nickName(entity.getNickName())
                .socialProvider(entity.getSocialProvider())
                .favoriteTeamCode(entity.getBaseballTeam().getCode())
                .favoriteTeamName(entity.getBaseballTeam().getTeamNameKr())
                .gender(entity.getGender() != null ? entity.getGender().name() : null)
                .age(entity.getAge())
                .status(entity.getStatus().name())
                .role(entity.getRole().name())
                .build();
    }

    public static UserEntity toEntity(UserDto dto, BaseballTeamEntity baseballTeamEntity) {
        if (dto == null) {
            return null;
        }

        UserEntity.GenderType genderType = getGenderType(dto);

        return UserEntity.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .socialId(dto.getSocialId())
                .nickName(dto.getNickName())
                .socialProvider(dto.getSocialProvider())
                .baseballTeam(baseballTeamEntity)
                .gender(genderType)
                .age(dto.getAge())
                .build();
    }

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .socialId(user.getSocialId())
                .nickName(user.getNickName())
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
                .nickName(user.getNickName())
                .socialProvider(user.getSocialProvider())
                .favoriteTeamCode(user.getBaseballTeam().getCode())
                .favoriteTeamName(user.getBaseballTeam().getTeamNameKr())
                .role(user.getRole().name())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .age(user.getAge())
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
}
