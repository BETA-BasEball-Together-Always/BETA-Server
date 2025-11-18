package com.beta.application.community.service;

import com.beta.common.exception.comment.CommentAccessDeniedException;
import com.beta.common.exception.comment.CommentDepthExceededException;
import com.beta.common.exception.comment.CommentNotFoundException;
import com.beta.common.exception.post.PostNotFoundException;
import com.beta.infra.community.entity.CommentEntity;
import com.beta.infra.community.entity.CommentLikeEntity;
import com.beta.infra.community.entity.PostEntity;
import com.beta.infra.community.repository.CommentJpaRepository;
import com.beta.infra.community.repository.CommentLikeJpaRepository;
import com.beta.infra.community.repository.PostJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentWriteService {

    private final CommentJpaRepository commentJpaRepository;
    private final PostJpaRepository postJpaRepository;
    private final CommentLikeJpaRepository commentLikeJpaRepository;

    @Transactional
    public void saveComment(Long postId, Long userId, String content, Long parentId) {
        postJpaRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        int depth = 0;
        if (parentId != null) {
            CommentEntity parentComment = commentJpaRepository.findById(parentId)
                    .orElseThrow(CommentNotFoundException::new);
            if (parentComment.getDepth() != 0) {
                throw new CommentDepthExceededException();
            }
            depth = 1;
        }

        CommentEntity comment = CommentEntity.builder()
                .postId(postId)
                .userId(userId)
                .content(content)
                .parentId(parentId)
                .depth(depth)
                .build();

        commentJpaRepository.save(comment);
        postJpaRepository.updateCommentCount(postId, 1);
    }

    @Transactional
    public void updateComment(Long commentId, Long userId, String content) {
        CommentEntity comment = commentJpaRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        if (!comment.getUserId().equals(userId)) {
            throw new CommentAccessDeniedException();
        }

        comment.updateContent(content);
        commentJpaRepository.save(comment);
    }

    @Transactional
    public void softDeleteComment(Long commentId, Long userId) {
        CommentEntity comment = commentJpaRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        if (!comment.getUserId().equals(userId)) {
            throw new CommentAccessDeniedException();
        }

        comment.softDelete();
        commentJpaRepository.save(comment);
        postJpaRepository.updateCommentCount(comment.getPostId(), -1);
    }

    @Transactional
    public void toggleLike(Long commentId, Long userId) {
        // Comment 존재 여부 검증
        if (!commentJpaRepository.existsById(commentId)) {
            throw new CommentNotFoundException();
        }

        // 기존 좋아요 조회
        CommentLikeEntity existingLike = commentLikeJpaRepository.findByCommentIdAndUserId(commentId, userId).orElse(null);

        if (existingLike != null) {
            // 좋아요 취소 (토글)
            commentLikeJpaRepository.delete(existingLike);
            commentJpaRepository.updateLikeCount(commentId, -1);
        } else {
            // 좋아요 생성
            CommentLikeEntity newLike = CommentLikeEntity.builder()
                    .commentId(commentId)
                    .userId(userId)
                    .build();
            commentLikeJpaRepository.save(newLike);
            commentJpaRepository.updateLikeCount(commentId, 1);
        }
    }
}
