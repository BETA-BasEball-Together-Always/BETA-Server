package com.beta.application.community.service;

import com.beta.common.exception.image.ImageCountExceededException;
import com.beta.infra.community.entity.PostImageEntity;
import com.beta.infra.community.entity.Status;
import com.beta.infra.community.repository.PostImageJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostImageReadService {

    private final PostImageJpaRepository postImageJpaRepository;

    public void validatePostImage(Long postId, int size) {
        long count = postImageJpaRepository.countByPostIdAndStatus(postId, Status.ACTIVE);
        if((count + size) > 5){
            throw new ImageCountExceededException();
        }
    }

    public int getStartOrder(Long postId) {
        PostImageEntity postImage = postImageJpaRepository.findTopByPostIdAndStatusOrderBySortDesc(postId, Status.ACTIVE).orElse(null);
        return postImage != null ? postImage.getSort() + 1 : 1;
    }
}
