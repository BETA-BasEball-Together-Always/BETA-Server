package com.beta.application.community.service;

import com.beta.common.exception.post.HashtagCountExceededException;
import com.beta.common.exception.post.PostAccessDeniedException;
import com.beta.common.exception.post.PostNotFoundException;
import com.beta.infra.community.entity.*;
import com.beta.infra.community.repository.HashtagJpaRepository;
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
    private final HashtagJpaRepository hashtagJpaRepository;

    @Transactional
    public void savePost(Long userId, Boolean allChannel, String content, String teamCode, List<String> hashtags, List<PostCreateRequest.Image> images) {
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
    public void updatePostContentAndHashtags(Long userId, Long postId, String content, List<String> addHashtags, List<Long> deleteHashtagIds) {
        PostEntity post = postJpaRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        if(!post.getUserId().equals(userId)) {
            throw new PostAccessDeniedException();
        }

        updateHashtags(postId, addHashtags, deleteHashtagIds);
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

    private void updateHashtags(Long postId, List<String> addHashtags, List<Long> deleteHashtagIds) {
        if((deleteHashtagIds == null || deleteHashtagIds.isEmpty()) && (addHashtags == null || addHashtags.isEmpty())) {
            return;
        }

        List<PostHashtagEntity> currentPostHashtags = postHashtagJpaRepository.findByPostId(postId);

        int finalCount = currentPostHashtags.size()
                         - (deleteHashtagIds != null ? deleteHashtagIds.size() : 0)
                         + (addHashtags != null ? addHashtags.size() : 0);
        if(finalCount > 5) {
            throw new HashtagCountExceededException();
        }

        if(deleteHashtagIds != null && !deleteHashtagIds.isEmpty()) {
            Set<Long> deleteSet = Set.copyOf(deleteHashtagIds);
            List<PostHashtagEntity> toBeDeleted = currentPostHashtags.stream()
                    .filter(entity -> deleteSet.contains(entity.getId()))
                    .toList();

            List<Long> deletedHashtagIds = toBeDeleted.stream()
                    .map(PostHashtagEntity::getHashtagId)
                    .toList();

            List<HashtagEntity> hashtagsToDecrement = hashtagJpaRepository.findAllById(deletedHashtagIds);
            hashtagsToDecrement.forEach(HashtagEntity::decrementUsageCount);
            hashtagJpaRepository.saveAll(hashtagsToDecrement);

            postHashtagJpaRepository.deleteAll(toBeDeleted);
        }

        if(addHashtags != null && !addHashtags.isEmpty()) {
            saveHashtags(postId, addHashtags);
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

    private void saveHashtags(Long postId, List<String> hashtags) {
        if(hashtags == null || hashtags.isEmpty()) {
            return;
        }
        if(hashtags.size() > 5) {
            throw new HashtagCountExceededException();
        }

        List<HashtagEntity> existingHashtags = hashtagJpaRepository.findByTagNameIn(hashtags);
        Map<String, Long> existingHashtagMap = existingHashtags.stream()
                .collect(Collectors.toMap(HashtagEntity::getTagName, HashtagEntity::getId));

        existingHashtags.forEach(HashtagEntity::incrementUsageCount);
        hashtagJpaRepository.saveAll(existingHashtags);

        List<String> newHashtagNames = hashtags.stream()
                .filter(name -> !existingHashtagMap.containsKey(name))
                .toList();

        List<HashtagEntity> newHashtags = newHashtagNames.stream()
                .map(name -> HashtagEntity.builder()
                        .tagName(name)
                        .build())
                .toList();

        if (!newHashtags.isEmpty()) {
            List<HashtagEntity> savedNewHashtags = hashtagJpaRepository.saveAll(newHashtags);
            savedNewHashtags.forEach(hashtag ->
                existingHashtagMap.put(hashtag.getTagName(), hashtag.getId())
            );
        }

        List<PostHashtagEntity> postHashtags = hashtags.stream()
                .map(name -> PostHashtagEntity.builder()
                        .postId(postId)
                        .hashtagId(existingHashtagMap.get(name))
                        .build())
                .toList();

        postHashtagJpaRepository.saveAll(postHashtags);
    }
}
