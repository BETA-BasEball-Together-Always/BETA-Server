package com.beta.infra.community.repository;

import com.beta.infra.community.entity.PostImageEntity;
import com.beta.infra.community.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageJpaRepository extends JpaRepository<PostImageEntity, Long> {
    long countByPostIdAndStatus(Long postId, Status status);

    List<PostImageEntity> findAllByIdInAndPostIdAndStatus(List<Long> imageIds, Long postId, Status status);

    List<PostImageEntity> findAllByIdInAndStatus(List<Long> imageIds, Status status);

    List<PostImageEntity> findAllByPostIdAndStatus(Long postId, Status status);

    List<PostImageEntity> findAllByPostIdAndStatusIn(Long postId, List<Status> status);
}
