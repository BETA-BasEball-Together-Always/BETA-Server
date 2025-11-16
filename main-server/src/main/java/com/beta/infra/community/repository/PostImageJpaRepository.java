package com.beta.infra.community.repository;

import com.beta.infra.community.entity.PostImageEntity;
import com.beta.infra.community.entity.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostImageJpaRepository extends JpaRepository<PostImageEntity, Long> {
    long countByPostIdAndStatus(Long postId, Status status);

    List<PostImageEntity> findAllByIdInAndPostIdAndStatus(List<Long> imageIds, Long postId, Status status);

    List<PostImageEntity> findAllByPostIdAndStatusIn(Long postId, List<Status> status);

    @Modifying
    @Query("UPDATE PostImageEntity p SET p.postId = :postId, p.sort = :sort, p.status = :status WHERE p.id = :imageId")
    void updateImagePostIdAndSortAndActive(@Param("postId") Long postId,@Param("imageId") Long imageId,@Param("sort") Integer sort,@Param("status") Status status);

    @Modifying
    @Query("UPDATE PostImageEntity p SET p.status = :status WHERE p.postId = :postId AND p.status = :condition")
    void updateImageStatus(Long postId, Status condition, Status status);
}
