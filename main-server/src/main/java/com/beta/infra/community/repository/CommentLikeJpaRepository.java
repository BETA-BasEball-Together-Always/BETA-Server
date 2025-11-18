package com.beta.infra.community.repository;

import com.beta.infra.community.entity.CommentLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeJpaRepository extends JpaRepository<CommentLikeEntity, Long> {

    Optional<CommentLikeEntity> findByCommentIdAndUserId(Long commentId, Long userId);
}
