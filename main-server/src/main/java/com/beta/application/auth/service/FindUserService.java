package com.beta.application.auth.service;

import com.beta.application.auth.mapper.UserMapper;
import com.beta.common.provider.SocialProvider;
import com.beta.domain.auth.User;
import com.beta.infra.auth.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindUserService {

    private final UserJpaRepository userJpaRepository;

    @Transactional(readOnly = true)
    public User findUserBySocialId(String socialId, SocialProvider socialProvider) {
        return UserMapper.toDomain(userJpaRepository.findBySocialIdAndSocialProvider(socialId, socialProvider).orElse(null));
    }

    @Transactional(readOnly = true)
    public boolean isNameDuplicate(String name) {
        return userJpaRepository.existsByName(name);
    }
}
