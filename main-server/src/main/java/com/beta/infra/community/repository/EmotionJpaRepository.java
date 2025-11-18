package com.beta.infra.community.repository;

import com.beta.infra.community.entity.EmotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface EmotionJpaRepository extends JpaRepository<EmotionEntity, Long> {

    Optional<EmotionEntity> findByPostIdAndUserId(Long postId, Long userId);
}
