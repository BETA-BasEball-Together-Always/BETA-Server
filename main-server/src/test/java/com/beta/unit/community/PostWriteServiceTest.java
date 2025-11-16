package com.beta.unit.community;

import com.beta.application.community.service.PostWriteService;
import com.beta.common.exception.post.HashtagCountExceededException;
import com.beta.common.exception.post.PostAccessDeniedException;
import com.beta.common.exception.post.PostNotFoundException;
import com.beta.infra.community.entity.HashtagEntity;
import com.beta.infra.community.entity.PostEntity;
import com.beta.infra.community.repository.HashtagJpaRepository;
import com.beta.infra.community.repository.PostHashtagRepository;
import com.beta.infra.community.repository.PostImageJpaRepository;
import com.beta.infra.community.repository.PostJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostWriteService 단위 테스트")
class PostWriteServiceTest {

    @Mock
    private PostJpaRepository postJpaRepository;
    @Mock
    private PostHashtagRepository postHashtagRepository;
    @Mock
    private PostImageJpaRepository postImageJpaRepository;
    @Mock
    private HashtagJpaRepository hashtagJpaRepository;

    @InjectMocks
    private PostWriteService postWriteService;


    @Test
    @DisplayName("게시글 저장 - 정상 동작")
    void savePost_success() {
        // given
        PostEntity savedPost = mock(PostEntity.class);
        when(savedPost.getId()).thenReturn(1L);
        when(postJpaRepository.save(any(PostEntity.class))).thenReturn(savedPost);

        // when
        postWriteService.savePost(1L, false, "내용", "DOOSAN", null, null);

        // then
        verify(postJpaRepository).save(any(PostEntity.class));
    }

    @Test
    @DisplayName("게시글 저장 - 해시태그 6개 초과시 예외")
    void savePost_throwsException_when_hashtagsExceed5() {
        // given
        List<String> tooManyHashtags = List.of("1", "2", "3", "4", "5", "6");

        PostEntity savedPost = mock(PostEntity.class);
        when(savedPost.getId()).thenReturn(1L);
        when(postJpaRepository.save(any())).thenReturn(savedPost);

        // when & then
        assertThatThrownBy(() -> postWriteService.savePost(1L, false, "내용", "DOOSAN", tooManyHashtags, null))
                .isInstanceOf(HashtagCountExceededException.class);
    }

    @Test
    @DisplayName("게시글 저장 - 해시태그 있으면 upsert 호출됨")
    void savePost_callsUpsert_when_hashtagsProvided() {
        // given
        PostEntity savedPost = mock(PostEntity.class);
        when(savedPost.getId()).thenReturn(1L);
        when(postJpaRepository.save(any())).thenReturn(savedPost);

        HashtagEntity hashtag = mock(HashtagEntity.class);
        when(hashtag.getId()).thenReturn(10L);
        when(hashtag.getTagName()).thenReturn("야구");
        when(hashtagJpaRepository.findByTagNameIn(any())).thenReturn(List.of(hashtag));

        List<String> hashtags = List.of("야구");

        // when
        postWriteService.savePost(1L, false, "내용", "DOOSAN", hashtags, null);

        // then
        verify(hashtagJpaRepository).upsertHashTags("야구");
        verify(postHashtagRepository).saveAll(any());
    }


    @Test
    @DisplayName("게시글 수정 - 존재하지 않는 게시글이면 예외")
    void updatePost_throwsException_when_postNotFound() {
        // given
        when(postJpaRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postWriteService.updatePost(1L, 999L, "내용", null, null, null))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("게시글 수정 - 권한 없으면 예외")
    void updatePost_throwsException_when_notOwner() {
        // given
        PostEntity post = mock(PostEntity.class);
        when(post.getUserId()).thenReturn(2L); // 다른 사용자
        when(postJpaRepository.findById(1L)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postWriteService.updatePost(1L, 1L, "내용", null, null, null))
                .isInstanceOf(PostAccessDeniedException.class);
    }

    @Test
    @DisplayName("게시글 수정 - 해시태그 추가시 개수 초과하면 예외")
    void updatePost_throwsException_when_hashtagCountExceeds() {
        // given
        PostEntity post = mock(PostEntity.class);
        when(post.getUserId()).thenReturn(1L);
        when(postJpaRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postHashtagRepository.countByPostId(1L)).thenReturn(5L); // 이미 5개

        List<String> newHashtags = List.of("추가"); // 1개 더 추가 시도

        // when & then
        assertThatThrownBy(() -> postWriteService.updatePost(1L, 1L, "내용", newHashtags, null, null))
                .isInstanceOf(HashtagCountExceededException.class);
    }

    @Test
    @DisplayName("게시글 수정 - 정상 동작")
    void updatePost_success() {
        // given
        PostEntity post = mock(PostEntity.class);
        when(post.getUserId()).thenReturn(1L);
        when(postJpaRepository.findById(1L)).thenReturn(Optional.of(post));

        // when
        postWriteService.updatePost(1L, 1L, "수정된 내용", null, null, null);

        // then
        verify(post).updateContent("수정된 내용");
        verify(postJpaRepository).save(post);
    }

    // ========== softDeletePost 테스트 ==========

    @Test
    @DisplayName("게시글 삭제 - 존재하지 않는 게시글이면 예외")
    void deletePost_throwsException_when_postNotFound() {
        // given
        when(postJpaRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postWriteService.softDeletePost(999L, 1L))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("게시글 삭제 - 권한 없으면 예외")
    void deletePost_throwsException_when_notOwner() {
        // given
        PostEntity post = mock(PostEntity.class);
        when(post.getUserId()).thenReturn(2L);
        when(postJpaRepository.findById(1L)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postWriteService.softDeletePost(1L, 1L))
                .isInstanceOf(PostAccessDeniedException.class);
    }

    @Test
    @DisplayName("게시글 삭제 - 정상 동작 (Soft Delete)")
    void deletePost_success() {
        // given
        PostEntity post = mock(PostEntity.class);
        when(post.getUserId()).thenReturn(1L);
        when(postJpaRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postImageJpaRepository.findAllByPostIdAndStatusIn(any(), any())).thenReturn(List.of());

        // when
        postWriteService.softDeletePost(1L, 1L);

        // then
        verify(post).softDelete();
        verify(postJpaRepository).save(post);
    }
}
