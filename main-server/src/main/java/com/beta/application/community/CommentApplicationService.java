package com.beta.application.community;

import com.beta.application.auth.service.UserReadService;
import com.beta.application.community.service.CommentReadService;
import com.beta.application.community.service.CommentWriteService;
import com.beta.presentation.community.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentApplicationService {

    private final CommentWriteService commentWriteService;
    private final CommentReadService commentReadService;
    private final UserReadService userReadService;

    public CommentResponse createComment(Long postId, String content, Long parentId, Long userId) {
        userReadService.findUserById(userId); // 사용자 존재 여부 확인
        commentWriteService.saveComment(postId, userId, content, parentId);
        return CommentResponse.success();
    }

    public CommentResponse updateComment(Long commentId, String content, Long userId) {
        commentWriteService.updateComment(commentId, userId, content);
        return CommentResponse.success();
    }

    public CommentResponse deleteComment(Long commentId, Long userId) {
        commentWriteService.softDeleteComment(commentId, userId);
        return CommentResponse.success();
    }

    public CommentResponse commentLike(Long commentId, Long userId) {
        commentWriteService.toggleLike(commentId, userId);
        return CommentResponse.success();
    }
}
