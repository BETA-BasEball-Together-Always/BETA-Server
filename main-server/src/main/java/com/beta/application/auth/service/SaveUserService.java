package com.beta.application.auth.service;

import com.beta.application.auth.dto.UserDto;
import com.beta.application.auth.mapper.UserMapper;
import com.beta.infra.auth.entity.UserConsentEntity;
import com.beta.infra.auth.entity.UserEntity;
import com.beta.infra.auth.repository.UserConsentJpaRepository;
import com.beta.infra.auth.repository.UserJpaRepository;
import com.beta.infra.common.entity.BaseballTeamEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaveUserService {

    private final UserJpaRepository userJpaRepository;
    private final UserConsentJpaRepository userConsentJpaRepository;

    public UserDto saveUser(UserDto userDto, BaseballTeamEntity baseballTeamEntity) {
        UserEntity userEntity = UserMapper.toEntity(userDto, baseballTeamEntity);
        return UserMapper.toDto(userJpaRepository.save(userEntity));
    }

    public void saveAgreements(Boolean agreeMarketing, Boolean agreePersonalInfo, Long id) {
        userConsentJpaRepository.save(UserConsentEntity.builder()
                .userId(id)
                .agreeMarketing(agreeMarketing)
                .agreePersonalInfo(agreePersonalInfo)
                .build());
    }
}
