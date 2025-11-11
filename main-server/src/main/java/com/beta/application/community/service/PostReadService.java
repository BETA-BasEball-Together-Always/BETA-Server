package com.beta.application.community.service;

import com.beta.application.community.dto.PostWithImagesDto;
import com.beta.common.exception.post.PostAccessDeniedException;
import com.beta.common.exception.post.PostNotFoundException;
import com.beta.infra.community.entity.PostEntity;
import com.beta.infra.community.repository.PostJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostReadService {

    private final PostJpaRepository postJpaRepository;

    public void validatePostOwnership(Long postId, Long userId) {
        PostEntity post = postJpaRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        if (post.getUserId() == null || !post.getUserId().equals(userId)) {
            throw new PostAccessDeniedException();
        }
    }

    public PostWithImagesDto getPostWithImages(Long postId) {
        return PostWithImagesDto.from(postJpaRepository.findPostWithImages(postId).orElseThrow(PostNotFoundException::new));
    }
}
