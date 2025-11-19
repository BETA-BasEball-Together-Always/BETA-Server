package com.beta.application.auth.service;

import com.beta.application.auth.dto.UserDto;
import com.beta.application.auth.mapper.UserMapper;
import com.beta.infra.auth.entity.UserConsentEntity;
import com.beta.infra.auth.entity.UserEntity;
import com.beta.infra.auth.repository.UserConsentJpaRepository;
import com.beta.infra.auth.repository.UserJpaRepository;
import com.beta.infra.common.entity.BaseballTeamEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserWriteService {

    private final UserJpaRepository userJpaRepository;
    private final UserConsentJpaRepository userConsentJpaRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto saveUser(UserDto userDto, BaseballTeamEntity baseballTeamEntity) {
        UserDto encryptedUserDto = UserDto.builder()
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .socialId(userDto.getSocialId())
                .nickName(userDto.getNickName())
                .socialProvider(userDto.getSocialProvider())
                .favoriteTeamCode(userDto.getFavoriteTeamCode())
                .favoriteTeamName(userDto.getFavoriteTeamName())
                .role(userDto.getRole())
                .gender(userDto.getGender())
                .age(userDto.getAge())
                .build();

        UserEntity userEntity = UserMapper.toEntity(encryptedUserDto, baseballTeamEntity);
        return UserMapper.toDto(userJpaRepository.save(userEntity));
    }

    public void saveAgreements(Boolean agreeMarketing, Boolean personalInfoRequired, Long id) {
        userConsentJpaRepository.save(UserConsentEntity.builder()
                .userId(id)
                .agreeMarketing(agreeMarketing)
                .personalInfoRequired(personalInfoRequired)
                .build());
    }
}
