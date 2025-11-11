package com.beta.unit.community;

import com.beta.application.community.service.PostWriteService;
import com.beta.infra.community.entity.PostEntity;
import com.beta.infra.community.entity.PostHashtagEntity;
import com.beta.infra.community.repository.PostHashtagRepository;
import com.beta.infra.community.repository.PostJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostWriteService 단위 테스트")
class PostWriteServiceTest {

    @Mock
    private PostJpaRepository postJpaRepository;

    @Mock
    private PostHashtagRepository postHashtagRepository;

    @InjectMocks
    private PostWriteService postWriteService;

    @Test
    @DisplayName("게시글 저장 시 PostEntity를 저장하고 ID를 반환한다")
    void should_returnPostId_when_savePost() {
        // given
        Long userId = 1L;
        String content = "테스트 게시글";
        String teamCode = "DOOSAN";
        Boolean allChannel = false;

        PostEntity savedPost = mock(PostEntity.class);
        when(savedPost.getId()).thenReturn(100L);
        when(postJpaRepository.save(any(PostEntity.class))).thenReturn(savedPost);

        // when
        Long result = postWriteService.savePost(userId, allChannel, content, teamCode);

        // then
        assertThat(result).isEqualTo(100L);
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
        postWriteService.savePost(userId, allChannel, content, teamCode);

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
        postWriteService.savePost(userId, allChannel, content, teamCode);

        // then
        verify(postJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo(PostEntity.Channel.DOOSAN);
    }

    @Test
    @DisplayName("해시태그 저장 시 PostHashtagEntity 리스트를 저장한다")
    void should_savePostHashtagEntities_when_saveHashtags() {
        // given
        Long postId = 1L;
        List<Long> hashtagIds = List.of(10L, 20L, 30L);

        ArgumentCaptor<List<PostHashtagEntity>> captor = ArgumentCaptor.forClass(List.class);

        // when
        postWriteService.saveHashtags(postId, hashtagIds);

        // then
        verify(postHashtagRepository).saveAll(captor.capture());
        List<PostHashtagEntity> captured = captor.getValue();

        assertThat(captured).hasSize(3);
        assertThat(captured).allMatch(entity -> entity.getPostId().equals(postId));
        assertThat(captured).extracting(PostHashtagEntity::getHashtagId)
                .containsExactlyInAnyOrder(10L, 20L, 30L);
    }

    @Test
    @DisplayName("빈 해시태그 리스트로 저장 시에도 정상 처리된다")
    void should_saveEmptyList_when_saveHashtagsWithEmptyList() {
        // given
        Long postId = 1L;
        List<Long> emptyHashtagIds = List.of();

        ArgumentCaptor<List<PostHashtagEntity>> captor = ArgumentCaptor.forClass(List.class);

        // when
        postWriteService.saveHashtags(postId, emptyHashtagIds);

        // then
        verify(postHashtagRepository).saveAll(captor.capture());
        List<PostHashtagEntity> captured = captor.getValue();

        assertThat(captured).isEmpty();
    }
}
