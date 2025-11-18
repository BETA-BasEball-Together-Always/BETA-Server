package com.beta.infra.community.repository;

import com.beta.infra.community.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostJpaRepository extends JpaRepository<PostEntity, Long>, PostRepositoryCustom {

    @Modifying
    @Query("UPDATE PostEntity p SET p.commentCount = p.commentCount + :increment WHERE p.id = :postId")
    void updateCommentCount(@Param("postId") Long postId, @Param("increment") int increment);

    @Modifying
    @Query("UPDATE PostEntity p SET p.likeCount = p.likeCount + :increment WHERE p.id = :postId")
    void updateLikeCount(@Param("postId") Long postId, @Param("increment") int increment);

    @Modifying
    @Query("UPDATE PostEntity p SET p.sadCount = p.sadCount + :increment WHERE p.id = :postId")
    void updateSadCount(@Param("postId") Long postId, @Param("increment") int increment);

    @Modifying
    @Query("UPDATE PostEntity p SET p.funCount = p.funCount + :increment WHERE p.id = :postId")
    void updateFunCount(@Param("postId") Long postId, @Param("increment") int increment);

    @Modifying
    @Query("UPDATE PostEntity p SET p.hypeCount = p.hypeCount + :increment WHERE p.id = :postId")
    void updateHypeCount(@Param("postId") Long postId, @Param("increment") int increment);
}
