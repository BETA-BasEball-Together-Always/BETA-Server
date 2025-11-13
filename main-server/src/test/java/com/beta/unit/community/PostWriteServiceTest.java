package com.beta.unit.community;

import com.beta.application.community.service.PostWriteService;
import com.beta.common.exception.post.HashtagCountExceededException;
import com.beta.common.exception.post.PostAccessDeniedException;
import com.beta.common.exception.post.PostNotFoundException;
import com.beta.infra.community.entity.PostEntity;
import com.beta.infra.community.entity.PostHashtagEntity;
import com.beta.infra.community.entity.PostImageEntity;
import com.beta.infra.community.repository.PostHashtagRepository;
import com.beta.infra.community.repository.PostImageJpaRepository;
import com.beta.infra.community.repository.PostJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @InjectMocks
    private PostWriteService postWriteService;

    @Test
    @DisplayName("게시글 저장 시 PostEntity를 저장한다")
    void should_savePost_when_savePost() {
        // given
        Long userId = 1L;
        String content = "테스트 게시글";
        String teamCode = "DOOSAN";
        Boolean allChannel = false;

        PostEntity savedPost = mock(PostEntity.class);
        when(savedPost.getId()).thenReturn(100L);
        when(postJpaRepository.save(any(PostEntity.class))).thenReturn(savedPost);

        // when
        postWriteService.savePost(userId, allChannel, content, teamCode, null, null);

        // then
        verify(postJpaRepository).save(any(PostEntity.class));
    }

    @Test
    @DisplayName("allChannel이 true일 때 channel을 ALL로 설정한다")
    void should_setChannelToAll_when_savePostWithAllChannelTrue() {
        // given
        Long userId = 1L;
        String content = "전체 채널 게시글";
        String teamCode = "DOOSAN";
        Boolean allChannel = true;

        PostEntity savedPost = mock(PostEntity.class);
        when(savedPost.getId()).thenReturn(100L);
        when(postJpaRepository.save(any(PostEntity.class))).thenReturn(savedPost);

        ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);

        // when
        postWriteService.savePost(userId, allChannel, content, teamCode, null, null);

        // then
        verify(postJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo(PostEntity.Channel.ALL);
    }

    @Test
    @DisplayName("allChannel이 false일 때 channel을 teamCode로 설정한다")
    void should_setChannelToTeamCode_when_savePostWithAllChannelFalse() {
        // given
        Long userId = 1L;
        String content = "팀 채널 게시글";
        String teamCode = "doosan";
        Boolean allChannel = false;

        PostEntity savedPost = mock(PostEntity.class);
        when(savedPost.getId()).thenReturn(100L);
        when(postJpaRepository.save(any(PostEntity.class))).thenReturn(savedPost);

        ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);

        // when
        postWriteService.savePost(userId, allChannel, content, teamCode, null, null);

        // then
        verify(postJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo(PostEntity.Channel.DOOSAN);
    }

    @Test
    @DisplayName("게시글 수정 시 권한이 없으면 PostAccessDeniedException을 발생시킨다")
    void should_throwPostAccessDeniedException_when_updatePostWithoutPermission() {
        // given
        Long userId = 1L;
        Long postId = 100L;
        Long otherUserId = 2L;
        String newContent = "수정된 내용";

        PostEntity post = mock(PostEntity.class);
        when(post.getUserId()).thenReturn(otherUserId);
        when(postJpaRepository.findById(postId)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postWriteService.updatePostContentAndHashtags(userId, postId, newContent, null, null))
                .isInstanceOf(PostAccessDeniedException.class);
    }

    @Test
    @DisplayName("게시글 수정 시 존재하지 않는 게시글이면 PostNotFoundException을 발생시킨다")
    void should_throwPostNotFoundException_when_updateNonExistentPost() {
        // given
        Long userId = 1L;
        Long postId = 999L;
        String newContent = "수정된 내용";

        when(postJpaRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postWriteService.updatePostContentAndHashtags(userId, postId, newContent, null, null))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("게시글 수정 시 내용을 업데이트한다")
    void should_updateContent_when_updatePost() {
        // given
        Long userId = 1L;
        Long postId = 100L;
        String newContent = "수정된 내용";

        PostEntity post = mock(PostEntity.class, RETURNS_DEEP_STUBS);
        when(post.getUserId()).thenReturn(userId);
        when(postJpaRepository.findById(postId)).thenReturn(Optional.of(post));

        // when
        postWriteService.updatePostContentAndHashtags(userId, postId, newContent, null, null);

        // then
        verify(post).updateContent(newContent);
        verify(postJpaRepository).save(post);
        // 해시태그가 null이면 findByPostId가 호출되지 않음
        verify(postHashtagRepository, never()).findByPostId(any());
    }

    @Test
    @DisplayName("게시글 수정 시 해시태그를 삭제할 수 있다")
    void should_deleteHashtags_when_updatePostWithDeleteHashtags() {
        // given
        Long userId = 1L;
        Long postId = 100L;
        String newContent = "수정된 내용";
        List<Long> deleteHashtagIds = List.of(1L, 2L); // PostHashtagEntity의 ID

        PostEntity post = mock(PostEntity.class, RETURNS_DEEP_STUBS);
        when(post.getUserId()).thenReturn(userId);
        when(postJpaRepository.findById(postId)).thenReturn(Optional.of(post));

        PostHashtagEntity hashtag1 = mock(PostHashtagEntity.class);
        when(hashtag1.getId()).thenReturn(1L);
        when(hashtag1.getHashtagId()).thenReturn(100L);
        PostHashtagEntity hashtag2 = mock(PostHashtagEntity.class);
        when(hashtag2.getId()).thenReturn(2L);
        when(hashtag2.getHashtagId()).thenReturn(200L);
        PostHashtagEntity hashtag3 = mock(PostHashtagEntity.class);
        when(hashtag3.getId()).thenReturn(3L);
        when(hashtag3.getHashtagId()).thenReturn(300L);

        when(postHashtagRepository.findByPostId(postId)).thenReturn(List.of(hashtag1, hashtag2, hashtag3));
        when(hashtagJpaRepository.findAllById(anyList())).thenReturn(List.of());

        // when
        postWriteService.updatePostContentAndHashtags(userId, postId, newContent, null, deleteHashtagIds);

        // then
        ArgumentCaptor<List<PostHashtagEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(postHashtagRepository).deleteAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    @Test
    @DisplayName("게시글 수정 시 해시태그를 추가할 수 있다")
    void should_addHashtags_when_updatePostWithNewHashtags() {
        // given
        Long userId = 1L;
        Long postId = 100L;
        String newContent = "수정된 내용";
        List<String> newHashtags = List.of("야구", "응원");

        PostEntity post = mock(PostEntity.class, RETURNS_DEEP_STUBS);
        when(post.getUserId()).thenReturn(userId);
        when(postJpaRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postHashtagRepository.findByPostId(postId)).thenReturn(List.of());

        // Mock existing hashtags
        HashtagEntity hashtag1 = mock(HashtagEntity.class);
        when(hashtag1.getTagName()).thenReturn("야구");
        when(hashtag1.getId()).thenReturn(10L);
        HashtagEntity hashtag2 = mock(HashtagEntity.class);
        when(hashtag2.getTagName()).thenReturn("응원");
        when(hashtag2.getId()).thenReturn(20L);

        when(hashtagJpaRepository.findByTagNameIn(newHashtags)).thenReturn(List.of(hashtag1, hashtag2));

        // when
        postWriteService.updatePostContentAndHashtags(userId, postId, newContent, newHashtags, null);

        // then
        ArgumentCaptor<List<PostHashtagEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(postHashtagRepository).saveAll(captor.capture());
        List<PostHashtagEntity> captured = captor.getValue();

        assertThat(captured).hasSize(2);
        assertThat(captured).extracting(PostHashtagEntity::getHashtagId)
                .containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    @DisplayName("해시태그가 5개를 초과하면 HashtagCountExceededException을 발생시킨다")
    void should_throwHashtagCountExceededException_when_hashtagCountExceeds5() {
        // given
        Long userId = 1L;
        Long postId = 100L;
        String newContent = "수정된 내용";

        // 현재 3개의 해시태그
        List<PostHashtagEntity> existingHashtags = List.of(
                mock(PostHashtagEntity.class),
                mock(PostHashtagEntity.class),
                mock(PostHashtagEntity.class)
        );

        // 3개를 추가하려고 시도 (총 6개, 5개 초과)
        List<String> newHashtags = List.of("야구", "응원", "KIA");

        PostEntity post = mock(PostEntity.class);
        when(post.getUserId()).thenReturn(userId);
        when(postJpaRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postHashtagRepository.findByPostId(postId)).thenReturn(existingHashtags);

        // when & then
        assertThatThrownBy(() -> postWriteService.updatePostContentAndHashtags(userId, postId, newContent, newHashtags, null))
                .isInstanceOf(HashtagCountExceededException.class);
    }

    @Test
    @DisplayName("게시글 삭제 시 소프트 삭제를 수행한다")
    void should_softDeletePost_when_deletePost() {
        // given
        Long userId = 1L;
        Long postId = 100L;

        PostEntity post = mock(PostEntity.class, RETURNS_DEEP_STUBS);
        when(post.getUserId()).thenReturn(userId);
        when(postJpaRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postImageJpaRepository.findAllByPostIdAndStatusIn(eq(postId), anyList())).thenReturn(List.of());

        // when
        postWriteService.softDeletePost(postId, userId);

        // then
        verify(post).softDelete();
        verify(postJpaRepository).save(post);
    }

    @Test
    @DisplayName("게시글 삭제 시 권한이 없으면 PostAccessDeniedException을 발생시킨다")
    void should_throwPostAccessDeniedException_when_deletePostWithoutPermission() {
        // given
        Long userId = 1L;
        Long postId = 100L;
        Long otherUserId = 2L;

        PostEntity post = mock(PostEntity.class);
        when(post.getUserId()).thenReturn(otherUserId);
        when(postJpaRepository.findById(postId)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postWriteService.softDeletePost(postId, userId))
                .isInstanceOf(PostAccessDeniedException.class);
    }

    @Test
    @DisplayName("게시글 삭제 시 존재하지 않는 게시글이면 PostNotFoundException을 발생시킨다")
    void should_throwPostNotFoundException_when_deleteNonExistentPost() {
        // given
        Long userId = 1L;
        Long postId = 999L;

        when(postJpaRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postWriteService.softDeletePost(postId, userId))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("게시글 삭제 시 연관된 이미지도 소프트 삭제한다")
    void should_softDeleteImages_when_deletePost() {
        // given
        Long userId = 1L;
        Long postId = 100L;

        PostEntity post = mock(PostEntity.class, RETURNS_DEEP_STUBS);
        when(post.getUserId()).thenReturn(userId);
        when(postJpaRepository.findById(postId)).thenReturn(Optional.of(post));

        PostImageEntity image1 = mock(PostImageEntity.class);
        PostImageEntity image2 = mock(PostImageEntity.class);
        List<PostImageEntity> images = List.of(image1, image2);
        when(postImageJpaRepository.findAllByPostIdAndStatusIn(eq(postId), anyList())).thenReturn(images);

        // when
        postWriteService.softDeletePost(postId, userId);

        // then
        verify(image1).softDelete();
        verify(image2).softDelete();
        verify(postImageJpaRepository).saveAll(images);
    }
}
