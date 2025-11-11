package com.beta.infra.community.repository;

import com.beta.infra.community.entity.HashtagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagJpaRepository extends JpaRepository<HashtagEntity, Long> {
}
