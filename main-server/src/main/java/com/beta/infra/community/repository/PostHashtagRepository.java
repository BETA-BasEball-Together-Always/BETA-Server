package com.beta.infra.community.repository;

import com.beta.infra.community.entity.PostHashtagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostHashtagRepository extends JpaRepository<PostHashtagEntity, Long> {
    List<PostHashtagEntity> findByPostId(Long postId);
}
