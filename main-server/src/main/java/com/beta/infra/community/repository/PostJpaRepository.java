package com.beta.infra.community.repository;

import com.beta.infra.community.entity.PostEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostJpaRepository extends JpaRepository<PostEntity, Long>, PostRepositoryCustom {

    @Modifying
    @Query("UPDATE PostEntity p SET p.commentCount = p.commentCount + :increment WHERE p.id = :postId")
    void updateCommentCount(@Param("postId") Long postId, @Param("increment") int increment);
}
