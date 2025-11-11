package com.beta.application.community.service;

import com.beta.common.exception.post.PostNotFoundException;
import com.beta.infra.community.entity.PostEntity;
import com.beta.infra.community.entity.PostHashtagEntity;
import com.beta.infra.community.repository.PostHashtagRepository;
import com.beta.infra.community.repository.PostJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostWriteService {

    private final PostJpaRepository postJpaRepository;
    private final PostHashtagRepository postHashtagJpaRepository;

    public Long savePost(Long userId, Boolean allChannel, String content, String teamCode) {
        String channel = teamCode.trim().toUpperCase();
        if(allChannel != null && allChannel) {
            channel = "ALL";
        }
        PostEntity savePost = PostEntity.builder()
                .content(content)
                .userId(userId)
                .channel(channel)
                .build();
        return postJpaRepository.save(savePost).getId();
    }

    @Transactional
    public void saveHashtags(Long postId, List<Long> hashtags) {
        List<PostHashtagEntity> hashtagEntities = hashtags.stream()
                .map(hashtagId -> PostHashtagEntity.builder()
                        .postId(postId)
                        .hashtagId(hashtagId)
                        .build())
                .toList();

        postHashtagJpaRepository.saveAll(hashtagEntities);
    }
}
