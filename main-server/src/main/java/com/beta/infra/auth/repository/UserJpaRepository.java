package com.beta.infra.auth.repository;

import com.beta.common.provider.SocialProvider;
import com.beta.infra.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findBySocialIdAndSocialProvider(String socialId, SocialProvider socialProvider);

    boolean existsByName(String name);
}
