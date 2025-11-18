package com.beta.infra.community.repository;

import com.beta.infra.community.entity.CommentEntity;
import com.beta.infra.community.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentJpaRepository extends JpaRepository<CommentEntity, Long> {

    @Modifying
    @Query("UPDATE CommentEntity c SET c.likeCount = c.likeCount + :increment WHERE c.id = :commentId")
    void updateLikeCount(@Param("commentId") Long commentId, @Param("increment") int increment);
}
