package com.beta.application.community.service;

import com.beta.common.exception.post.HashtagCountExceededException;
import com.beta.common.exception.post.PostAccessDeniedException;
import com.beta.common.exception.post.PostNotFoundException;
import com.beta.infra.community.entity.*;
import com.beta.infra.community.repository.PostHashtagRepository;
import com.beta.infra.community.repository.PostImageJpaRepository;
import com.beta.infra.community.repository.PostJpaRepository;
import com.beta.presentation.community.request.PostCreateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostWriteService {

    private final PostJpaRepository postJpaRepository;
    private final PostHashtagRepository postHashtagJpaRepository;
    private final PostImageJpaRepository postImageJpaRepository;

    @Transactional
    public void savePost(Long userId, Boolean allChannel, String content, String teamCode, List<Long> hashtags, List<PostCreateRequest.Image> images) {
        String channel = teamCode.trim().toUpperCase();
        if(allChannel != null && allChannel) {
            channel = "ALL";
        }
        PostEntity savePost = PostEntity.builder()
                .content(content)
                .userId(userId)
                .channel(channel)
                .build();
        Long postId = postJpaRepository.save(savePost).getId();
        saveHashtags(postId, hashtags);
        publishPostImages(postId, images);
    }

    @Transactional
    public void updatePostContentAndHashtags(Long userId, Long postId, String content, List<Long> hashtags, List<Long> deleteHashtags) {
        PostEntity post = postJpaRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        if(!post.getUserId().equals(userId)) {
            throw new PostAccessDeniedException();
        }

        updateHashtags(postId, hashtags, deleteHashtags);
        post.updateContent(content);
        postJpaRepository.save(post);
    }

    @Transactional
    public void softDeletePost(Long postId, Long userId) {
        PostEntity post = postJpaRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        if(!post.getUserId().equals(userId)) {
            throw new PostAccessDeniedException();
        }
        post.softDelete();
        postJpaRepository.save(post);
        imageDelete(postId);
    }

    private void imageDelete(Long postId) {
        List<PostImageEntity> postImageEntities = postImageJpaRepository.findAllByPostIdAndStatusIn(postId, List.of(Status.ACTIVE, Status.MARKED_FOR_DELETION));
        if(postImageEntities != null && !postImageEntities.isEmpty()) {
            postImageEntities.forEach(PostImageEntity::softDelete);
            postImageJpaRepository.saveAll(postImageEntities);
        }
    }

    private void updateHashtags(Long postId, List<Long> hashtags, List<Long> deleteHashtags) {
        if((deleteHashtags == null || deleteHashtags.isEmpty()) && (hashtags == null || hashtags.isEmpty())) {
            return;
        }
        List<PostHashtagEntity> hashtagEntities = postHashtagJpaRepository.findByPostId(postId);
        int hashtagCount = (hashtagEntities.size() - (deleteHashtags != null ? deleteHashtags.size() : 0)) + (hashtags != null ? hashtags.size() : 0);
        if(hashtagCount > 10) {
            throw new HashtagCountExceededException();
        }
        if(deleteHashtags != null && !deleteHashtags.isEmpty()) {
            Set<Long> deleteHashtagsSet = Set.copyOf(deleteHashtags);
            List<PostHashtagEntity> toBeDeleted = hashtagEntities.stream()
                    .filter(entity -> deleteHashtagsSet.contains(entity.getId()))
                    .toList();
            postHashtagJpaRepository.deleteAll(toBeDeleted);
        }
        if(hashtags != null && !hashtags.isEmpty()) {
            List<PostHashtagEntity> toBeAdded = hashtags.stream()
                    .map(hashtagId -> PostHashtagEntity.builder()
                            .postId(postId)
                            .hashtagId(hashtagId)
                            .build())
                    .toList();
            postHashtagJpaRepository.saveAll(toBeAdded);
        }
    }

    private void publishPostImages(Long postId, List<PostCreateRequest.Image> images) {
        if(images == null || images.isEmpty()) {
            return;
        }
        Map<Long, Integer> sortMap = images.stream()
                .collect(Collectors.toMap(PostCreateRequest.Image::getImageId, PostCreateRequest.Image::getSort));

        List<PostImageEntity> postImages = postImageJpaRepository.findAllByIdInAndStatus(new ArrayList<>(sortMap.keySet()), Status.PENDING);

        if(!postImages.isEmpty()) {
            postImages.forEach(image -> {
                image.imageActivateAndSort(postId, sortMap.get(image.getId()));
            });
            postImageJpaRepository.saveAll(postImages);
        }
    }

    private void saveHashtags(Long postId, List<Long> hashtags) {
        if(hashtags == null || hashtags.isEmpty()) {
            return;
        }
        List<PostHashtagEntity> hashtagEntities = hashtags.stream()
                .map(hashtagId -> PostHashtagEntity.builder()
                        .postId(postId)
                        .hashtagId(hashtagId)
                        .build())
                .toList();

        postHashtagJpaRepository.saveAll(hashtagEntities);
    }
}
