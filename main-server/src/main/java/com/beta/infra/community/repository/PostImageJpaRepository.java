package com.beta.infra.community.repository;

import com.beta.infra.community.entity.PostImageEntity;
import com.beta.infra.community.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostImageJpaRepository extends JpaRepository<PostImageEntity, Long> {
    long countByPostIdAndStatus(Long postId, Status status);

    Optional<PostImageEntity> findTopByPostIdAndStatusOrderBySortDesc(Long postId, Status status);

    List<PostImageEntity> findAllByIdInAndPostIdAndStatus(List<Long> imageIds, Long postId, Status status);

    List<PostImageEntity> findAllByIdInAndPostIdAndStatusIn(List<Long> imageIds, Long postId, List<Status> statuses);
}
