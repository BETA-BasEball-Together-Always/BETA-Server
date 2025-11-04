package com.beta.infra.community.repository;

import com.beta.infra.community.entity.PostImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageJpaRepository extends JpaRepository<PostImageEntity, Long> {
}
