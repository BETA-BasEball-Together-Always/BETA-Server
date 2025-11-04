package com.beta.infra.community.repository;

import com.beta.infra.community.entity.PostImageErrorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageErrorJpaRepository extends JpaRepository<PostImageErrorEntity, Long> {
}
