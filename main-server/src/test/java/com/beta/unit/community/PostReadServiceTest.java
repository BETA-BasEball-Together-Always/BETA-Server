package com.beta.unit.community;

import com.beta.application.community.service.PostReadService;
import com.beta.common.exception.post.PostAccessDeniedException;
import com.beta.common.exception.post.PostNotFoundException;
import com.beta.infra.community.entity.PostEntity;
import com.beta.infra.community.repository.PostJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostReadService 단위 테스트")
class PostReadServiceTest {

    @Mock
    private PostJpaRepository postJpaRepository;

    @InjectMocks
    private PostReadService postReadService;

    @Test
    @DisplayName("게시글 소유권 검증 시 정상 처리된다")
    void should_pass_when_validatePostOwnershipWithValidOwner() {
        // given
        Long postId = 1L;
        Long userId = 100L;

        PostEntity post = mock(PostEntity.class);
        when(post.getUserId()).thenReturn(userId);
        when(postJpaRepository.findById(postId)).thenReturn(Optional.of(post));

        // when & then
        assertThatCode(() -> postReadService.validatePostOwnership(postId, userId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 게시글 소유권 검증 시 PostNotFoundException을 발생시킨다")
    void should_throwPostNotFoundException_when_validatePostOwnershipWithNonExistentPost() {
        // given
        Long postId = 999L;
        Long userId = 100L;

        when(postJpaRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postReadService.validatePostOwnership(postId, userId))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("다른 사용자의 게시글 소유권 검증 시 PostAccessDeniedException을 발생시킨다")
    void should_throwPostAccessDeniedException_when_validatePostOwnershipWithDifferentUser() {
        // given
        Long postId = 1L;
        Long userId = 100L;
        Long otherUserId = 200L;

        PostEntity post = mock(PostEntity.class);
        when(post.getUserId()).thenReturn(otherUserId);
        when(postJpaRepository.findById(postId)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postReadService.validatePostOwnership(postId, userId))
                .isInstanceOf(PostAccessDeniedException.class)
                .hasMessage("게시글에 대한 권한이 없습니다.");
    }

    @Test
    @DisplayName("userId가 null인 게시글 소유권 검증 시 PostAccessDeniedException을 발생시킨다")
    void should_throwPostAccessDeniedException_when_validatePostOwnershipWithNullUserId() {
        // given
        Long postId = 1L;
        Long userId = 100L;

        PostEntity post = mock(PostEntity.class);
        when(post.getUserId()).thenReturn(null);
        when(postJpaRepository.findById(postId)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postReadService.validatePostOwnership(postId, userId))
                .isInstanceOf(PostAccessDeniedException.class)
                .hasMessage("게시글에 대한 권한이 없습니다.");
    }
}
