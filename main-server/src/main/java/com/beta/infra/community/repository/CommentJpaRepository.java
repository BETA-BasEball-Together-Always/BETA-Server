package com.beta.infra.community.repository;

import com.beta.infra.community.entity.CommentEntity;
import com.beta.infra.community.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentJpaRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findAllByPostIdAndStatus(Long postId, Status status);

    long countByPostIdAndStatus(Long postId, Status status);
}
