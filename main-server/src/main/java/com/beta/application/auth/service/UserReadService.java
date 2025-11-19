package com.beta.application.auth.service;

import com.beta.application.auth.mapper.UserMapper;
import com.beta.common.exception.auth.UserNotFoundException;
import com.beta.common.provider.SocialProvider;
import com.beta.domain.auth.User;
import com.beta.infra.auth.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserReadService {

    private final UserJpaRepository userJpaRepository;

    @Transactional(readOnly = true)
    public User findUserBySocialId(String socialId, SocialProvider socialProvider) {
        return UserMapper.toDomain(userJpaRepository.findBySocialIdAndSocialProvider(socialId, socialProvider).orElse(null));
    }

    @Transactional(readOnly = true)
    public boolean isNameDuplicate(String nickName) {
        return userJpaRepository.existsByNickName(nickName);
    }

    @Transactional(readOnly = true)
    public boolean isEmailDuplicate(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        return UserMapper.toDomain(userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId)));
    }

    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return UserMapper.toDomain(userJpaRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. email: " + email)));
    }
}
