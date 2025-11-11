package com.beta.infra.community.repository;

import com.beta.infra.community.repository.dao.PostWithImages;

import java.util.Optional;

public interface PostRepositoryCustom {
    Optional<PostWithImages> findPostWithImages(Long postId);
}
