package com.beta.application.community.service;

import com.beta.common.exception.post.HashtagCountExceededException;
import com.beta.common.exception.post.PostAccessDeniedException;
import com.beta.common.exception.post.PostNotFoundException;
import com.beta.infra.community.entity.*;
import com.beta.infra.community.repository.*;
import com.beta.presentation.community.request.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostWriteService {

    private final PostJpaRepository postJpaRepository;
    private final PostHashtagRepository postHashtagJpaRepository;
    private final PostImageJpaRepository postImageJpaRepository;
    private final HashtagJpaRepository hashtagJpaRepository;
    private final EmotionJpaRepository emotionJpaRepository;

    @Transactional
    public void savePost(Long userId, Boolean allChannel, String content, String teamCode, List<String> hashtags, List<Image> images) {
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
        if(hashtags != null && !hashtags.isEmpty()) {
            if(hashtags.size() > 5) {
                throw new HashtagCountExceededException();
            }
            saveHashtags(postId, hashtags);
        }
        if(images != null && !images.isEmpty()) {
            publishPostImages(postId, images);
        }
    }

    @Transactional
    public void updatePost(Long userId, Long postId, String content, List<String> addHashtags, List<Long> deleteHashtagIds, List<Image> images) {
        PostEntity post = postJpaRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        if(!post.getUserId().equals(userId)) {
            throw new PostAccessDeniedException();
        }

        if(deleteHashtagIds != null && !deleteHashtagIds.isEmpty()) {
            deleteHashtags(deleteHashtagIds);
        }
        if(addHashtags != null && !addHashtags.isEmpty()) {
            long total = postHashtagJpaRepository.countByPostId(postId);
            if(total + addHashtags.size() > 5) {
                throw new HashtagCountExceededException();
            }
            saveHashtags(postId, addHashtags);
        }

        if(images != null && !images.isEmpty()) {
            publishPostImages(postId, images);
        }

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

    private void deleteHashtags(List<Long> deleteHashtagIds) {
        List<Long> hashtagIds = postHashtagJpaRepository.findAllById(deleteHashtagIds).stream()
                .map(PostHashtagEntity::getHashtagId)
                .toList();
        postHashtagJpaRepository.deleteAllById(deleteHashtagIds);
        hashtagJpaRepository.updateCountDecrement(hashtagIds);
    }

    private void publishPostImages(Long postId, List<Image> images) {
        if(images == null || images.isEmpty()) {
            return;
        }
        for(Image image : images) {
            postImageJpaRepository.updateImagePostIdAndSortAndActive(postId, image.getImageId(), image.getSort(), Status.ACTIVE);
        }
        postImageJpaRepository.updateImageStatus(postId, Status.MARKED_FOR_DELETION, Status.DELETED);
    }

    private void saveHashtags(Long postId, List<String> hashtags) {
        List<String> sortedHashtags = hashtags.stream()
                .sorted()
                .toList();

        sortedHashtags.forEach(hashtagJpaRepository::upsertHashTags);

        Map<String, Long> hashtagIdMap = hashtagJpaRepository.findByTagNameIn(hashtags)
                .stream()
                .collect(Collectors.toMap(HashtagEntity::getTagName, HashtagEntity::getId));

        List<PostHashtagEntity> postHashtags = hashtags.stream()
                .map(name -> PostHashtagEntity.builder()
                        .postId(postId)
                        .hashtagId(hashtagIdMap.get(name))
                        .build())
                .toList();

        postHashtagJpaRepository.saveAll(postHashtags);
    }

    @Transactional
    public void updateEmotion(Long postId, String emotionType, Long userId) {
        if (!postJpaRepository.existsById(postId)) {
            throw new PostNotFoundException();
        }

        EmotionEntity.EmotionType newEmotionType = EmotionEntity.EmotionType.valueOf(emotionType.toUpperCase());

        EmotionEntity existingEmotion = emotionJpaRepository.findByPostIdAndUserId(postId, userId).orElse(null);

        if (existingEmotion != null) {
            if (existingEmotion.getEmotionType() == newEmotionType) {
                emotionJpaRepository.delete(existingEmotion);
                updateEmotionCount(postId, newEmotionType, -1);
            } else {
                updateEmotionCount(postId, existingEmotion.getEmotionType(), -1);
                existingEmotion.changeEmotionType(newEmotionType);
                emotionJpaRepository.save(existingEmotion);
                updateEmotionCount(postId, newEmotionType, 1);
            }
        } else {
            EmotionEntity newEmotion = EmotionEntity.builder()
                    .postId(postId)
                    .userId(userId)
                    .emotionType(newEmotionType)
                    .build();
            emotionJpaRepository.save(newEmotion);
            updateEmotionCount(postId, newEmotionType, 1);
        }
    }

    private void updateEmotionCount(Long postId, EmotionEntity.EmotionType emotionType, int increment) {
        switch (emotionType) {
            case LIKE -> postJpaRepository.updateLikeCount(postId, increment);
            case SAD -> postJpaRepository.updateSadCount(postId, increment);
            case FUN -> postJpaRepository.updateFunCount(postId, increment);
            case HYPE -> postJpaRepository.updateHypeCount(postId, increment);
        }
    }
}
