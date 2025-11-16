package com.beta.infra.community.repository;

import com.beta.infra.community.entity.PostHashtagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHashtagRepository extends JpaRepository<PostHashtagEntity, Long> {
    long countByPostId(Long postId);
}
