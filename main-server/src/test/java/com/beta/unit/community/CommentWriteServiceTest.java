package com.beta.unit.community;

import com.beta.application.community.service.CommentWriteService;
import com.beta.common.exception.comment.CommentAccessDeniedException;
import com.beta.common.exception.comment.CommentDepthExceededException;
import com.beta.common.exception.comment.CommentNotFoundException;
import com.beta.common.exception.post.PostNotFoundException;
import com.beta.infra.community.entity.CommentEntity;
import com.beta.infra.community.entity.PostEntity;
import com.beta.infra.community.repository.CommentJpaRepository;
import com.beta.infra.community.repository.PostJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentWriteService 단위 테스트")
class CommentWriteServiceTest {

    @Mock
    private CommentJpaRepository commentJpaRepository;

    @Mock
    private PostJpaRepository postJpaRepository;

    @InjectMocks
    private CommentWriteService commentWriteService;

    @Test
    @DisplayName("댓글 저장 - 정상 동작")
    void saveComment_success() {
        // given
        PostEntity mockPost = mock(PostEntity.class);
        when(postJpaRepository.findById(1L)).thenReturn(Optional.of(mockPost));

        // when
        commentWriteService.saveComment(1L, 1L, "댓글 내용", null);

        // then
        verify(commentJpaRepository).save(any(CommentEntity.class));
        verify(postJpaRepository).updateCommentCount(1L, 1);
    }

    @Test
    @DisplayName("대댓글 저장 - 정상 동작 (depth 1)")
    void saveReply_success() {
        // given
        PostEntity mockPost = mock(PostEntity.class);
        CommentEntity parentComment = mock(CommentEntity.class);

        when(postJpaRepository.findById(1L)).thenReturn(Optional.of(mockPost));
        when(commentJpaRepository.findById(10L)).thenReturn(Optional.of(parentComment));
        when(parentComment.getDepth()).thenReturn(0);

        // when
        commentWriteService.saveComment(1L, 1L, "대댓글 내용", 10L);

        // then
        verify(commentJpaRepository).save(any(CommentEntity.class));
        verify(postJpaRepository).updateCommentCount(1L, 1);
    }

    @Test
    @DisplayName("댓글 저장 - 게시글 없으면 예외")
    void saveComment_throwsException_when_postNotFound() {
        // given
        when(postJpaRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentWriteService.saveComment(999L, 1L, "내용", null))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("대댓글 저장 - 부모 댓글 없으면 예외")
    void saveReply_throwsException_when_parentNotFound() {
        // given
        PostEntity mockPost = mock(PostEntity.class);
        when(postJpaRepository.findById(1L)).thenReturn(Optional.of(mockPost));
        when(commentJpaRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentWriteService.saveComment(1L, 1L, "대댓글", 999L))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @DisplayName("대댓글의 대댓글 저장 - depth 초과 예외")
    void saveReply_throwsException_when_depthExceeded() {
        // given
        PostEntity mockPost = mock(PostEntity.class);
        CommentEntity parentComment = mock(CommentEntity.class);

        when(postJpaRepository.findById(1L)).thenReturn(Optional.of(mockPost));
        when(commentJpaRepository.findById(10L)).thenReturn(Optional.of(parentComment));
        when(parentComment.getDepth()).thenReturn(1); // 이미 대댓글

        // when & then
        assertThatThrownBy(() -> commentWriteService.saveComment(1L, 1L, "대댓글의 대댓글", 10L))
                .isInstanceOf(CommentDepthExceededException.class);
    }

    @Test
    @DisplayName("댓글 수정 - 정상 동작")
    void updateComment_success() {
        // given
        CommentEntity mockComment = mock(CommentEntity.class);
        when(commentJpaRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(mockComment.getUserId()).thenReturn(1L);

        // when
        commentWriteService.updateComment(1L, 1L, "수정된 내용");

        // then
        verify(mockComment).updateContent("수정된 내용");
        verify(commentJpaRepository).save(mockComment);
    }

    @Test
    @DisplayName("댓글 수정 - 댓글 없으면 예외")
    void updateComment_throwsException_when_commentNotFound() {
        // given
        when(commentJpaRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentWriteService.updateComment(999L, 1L, "내용"))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @DisplayName("댓글 수정 - 권한 없으면 예외")
    void updateComment_throwsException_when_notOwner() {
        // given
        CommentEntity mockComment = mock(CommentEntity.class);
        when(commentJpaRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(mockComment.getUserId()).thenReturn(2L); // 다른 사용자

        // when & then
        assertThatThrownBy(() -> commentWriteService.updateComment(1L, 1L, "내용"))
                .isInstanceOf(CommentAccessDeniedException.class);
    }

    @Test
    @DisplayName("댓글 삭제 - 정상 동작")
    void deleteComment_success() {
        // given
        CommentEntity mockComment = mock(CommentEntity.class);

        when(commentJpaRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(mockComment.getUserId()).thenReturn(1L);
        when(mockComment.getPostId()).thenReturn(10L);

        // when
        commentWriteService.softDeleteComment(1L, 1L);

        // then
        verify(mockComment).softDelete();
        verify(commentJpaRepository).save(mockComment);
        verify(postJpaRepository).updateCommentCount(10L, -1);
    }

    @Test
    @DisplayName("댓글 삭제 - 댓글 없으면 예외")
    void deleteComment_throwsException_when_commentNotFound() {
        // given
        when(commentJpaRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentWriteService.softDeleteComment(999L, 1L))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @DisplayName("댓글 삭제 - 권한 없으면 예외")
    void deleteComment_throwsException_when_notOwner() {
        // given
        CommentEntity mockComment = mock(CommentEntity.class);
        when(commentJpaRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(mockComment.getUserId()).thenReturn(2L); // 다른 사용자

        // when & then
        assertThatThrownBy(() -> commentWriteService.softDeleteComment(1L, 1L))
                .isInstanceOf(CommentAccessDeniedException.class);
    }
}
