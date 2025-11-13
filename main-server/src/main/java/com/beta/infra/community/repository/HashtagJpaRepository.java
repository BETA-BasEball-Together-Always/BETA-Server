package com.beta.infra.community.repository;

import com.beta.infra.community.entity.HashtagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HashtagJpaRepository extends JpaRepository<HashtagEntity, Long> {
    List<HashtagEntity> findByTagNameIn(List<String> hashtags);
    Optional<HashtagEntity> findByTagName(String tagName);
}
