package com.beta.integration.community;

import com.beta.application.community.PostApplicationService;
import com.beta.application.community.dto.HashtagDto;
import com.beta.common.docker.TestContainer;
import com.beta.common.exception.auth.UserNotFoundException;
import com.beta.common.exception.idempotency.IdempotencyKeyException;
import com.beta.common.fixture.PostFixture;
import com.beta.common.fixture.TeamFixture;
import com.beta.common.fixture.UserFixture;
import com.beta.infra.auth.entity.UserEntity;
import com.beta.infra.auth.repository.UserJpaRepository;
import com.beta.infra.common.entity.BaseballTeamEntity;
import com.beta.infra.common.repository.BaseballTeamRepository;
import com.beta.infra.community.entity.HashtagEntity;
import com.beta.infra.community.entity.PostEntity;
import com.beta.infra.community.entity.PostHashtagEntity;
import com.beta.infra.community.entity.PostImageEntity;
import com.beta.infra.community.entity.Status;
import com.beta.infra.community.gcs.GcsStorageClient;
import com.beta.infra.community.repository.HashtagJpaRepository;
import com.beta.infra.community.repository.PostHashtagRepository;
import com.beta.infra.community.repository.PostImageJpaRepository;
import com.beta.infra.community.repository.PostJpaRepository;
import com.beta.presentation.community.request.PostCreateRequest;
import com.beta.presentation.community.response.PostUploadResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("PostApplicationService 통합 테스트")
class PostApplicationServiceIntegrationTest extends TestContainer {

    @Autowired
    private PostApplicationService postApplicationService;

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private PostHashtagRepository postHashtagRepository;

    @Autowired
    private PostImageJpaRepository postImageJpaRepository;

    @Autowired
    private HashtagJpaRepository hashtagJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BaseballTeamRepository baseballTeamRepository;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private GcsStorageClient gcsStorageClient;

    private BaseballTeamEntity testTeam;
    private UserEntity testUser;
    private HashtagEntity hashtag1;
    private HashtagEntity hashtag2;

    @BeforeEach
    void setUp() {
        testTeam = TeamFixture.createDoosan();
        baseballTeamRepository.save(testTeam);

        testUser = UserFixture.createActiveUser("test_social_id", "테스트유저", testTeam);
        userJpaRepository.save(testUser);

        hashtag1 = PostFixture.createHashtag("야구");
        hashtag2 = PostFixture.createHashtag("응원");
        hashtagJpaRepository.saveAll(List.of(hashtag1, hashtag2));
    }

    @AfterEach
    void tearDown() {
        postHashtagRepository.deleteAll();
        postImageJpaRepository.deleteAll();
        postJpaRepository.deleteAll();
        hashtagJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        baseballTeamRepository.deleteAll();
    }

    @Test
    @DisplayName("게시글 업로드 시 Post를 저장하고 Hashtag를 연결한다")
    void should_savePostAndLinkHashtags_when_uploadPost() {
        // given
        String idempotencyKey = UUID.randomUUID().toString();
        PostCreateRequest request = new PostCreateRequest(
                "테스트 게시글 내용",
                false,
                null,
                List.of(hashtag1.getId(), hashtag2.getId())
        );

        // when
        PostUploadResponse response = postApplicationService.uploadPost(
                idempotencyKey, request, testUser.getId(), testTeam.getCode()
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();

        // DB 검증: Post가 저장되었는지
        List<PostEntity> savedPosts = postJpaRepository.findAll();
        assertThat(savedPosts).hasSize(1);
        assertThat(savedPosts.get(0).getContent()).isEqualTo("테스트 게시글 내용");
        assertThat(savedPosts.get(0).getUserId()).isEqualTo(testUser.getId());
        assertThat(savedPosts.get(0).getChannel()).isEqualTo(PostEntity.Channel.DOOSAN);

        // DB 검증: Hashtag가 연결되었는지
        List<PostHashtagEntity> savedHashtags = postHashtagRepository.findAll();
        assertThat(savedHashtags).hasSize(2);
        assertThat(savedHashtags).extracting(PostHashtagEntity::getHashtagId)
                .containsExactlyInAnyOrder(hashtag1.getId(), hashtag2.getId());
    }

    @Test
    @DisplayName("게시글 업로드 시 이미지를 PENDING에서 ACTIVE로 변경하고 postId를 연결한다")
    void should_publishPendingImages_when_uploadPostWithImages() {
        // given
        // 1. 먼저 PENDING 상태의 이미지 생성 (실제로는 이미지 업로드 API를 통해 생성됨)
        PostImageEntity pendingImage1 = PostImageEntity.builder()
                .postId(null)
                .userId(testUser.getId())
                .imgUrl("https://storage.googleapis.com/test/pending1.jpg")
                .originName("pending1.jpg")
                .newName("unique-pending1.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .sort(0)
                .status(Status.PENDING)
                .build();

        PostImageEntity pendingImage2 = PostImageEntity.builder()
                .postId(null)
                .userId(testUser.getId())
                .imgUrl("https://storage.googleapis.com/test/pending2.jpg")
                .originName("pending2.jpg")
                .newName("unique-pending2.jpg")
                .fileSize(2048L)
                .mimeType("image/jpeg")
                .sort(0)
                .status(Status.PENDING)
                .build();

        postImageJpaRepository.saveAll(List.of(pendingImage1, pendingImage2));

        // 2. 게시글 작성 요청 (이미지 ID와 정렬 순서 포함)
        String idempotencyKey = UUID.randomUUID().toString();
        PostCreateRequest request = new PostCreateRequest(
                "이미지가 있는 게시글",
                true,
                List.of(
                        new PostCreateRequest.Image(pendingImage1.getId(), 1),
                        new PostCreateRequest.Image(pendingImage2.getId(), 2)
                ),
                null
        );

        // when
        PostUploadResponse response = postApplicationService.uploadPost(
                idempotencyKey, request, testUser.getId(), testTeam.getCode()
        );

        // then
        assertThat(response.isSuccess()).isTrue();

        // DB 검증: Post가 저장되었는지
        List<PostEntity> savedPosts = postJpaRepository.findAll();
        assertThat(savedPosts).hasSize(1);
        Long postId = savedPosts.getFirst().getId();

        // DB 검증: 이미지가 ACTIVE 상태로 변경되고 postId가 연결되었는지
        List<PostImageEntity> images = postImageJpaRepository.findAll();
        assertThat(images).hasSize(2);
        assertThat(images).allMatch(img -> img.getPostId().equals(postId));
        assertThat(images).allMatch(img -> img.getStatus() == Status.ACTIVE);
        assertThat(images).extracting(PostImageEntity::getSort)
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    @DisplayName("allChannel이 true일 때 ALL 채널로 게시글을 생성한다")
    void should_createPostInAllChannel_when_allChannelIsTrue() {
        // given
        String idempotencyKey = UUID.randomUUID().toString();
        PostCreateRequest request = new PostCreateRequest(
                "전체 채널 게시글",
                true,
                null,
                null
        );

        // when
        postApplicationService.uploadPost(
                idempotencyKey, request, testUser.getId(), testTeam.getCode()
        );

        // then
        List<PostEntity> savedPosts = postJpaRepository.findAll();
        assertThat(savedPosts).hasSize(1);
        assertThat(savedPosts.get(0).getChannel()).isEqualTo(PostEntity.Channel.ALL);
    }

    @Test
    @DisplayName("allChannel이 false일 때 팀 채널로 게시글을 생성한다")
    void should_createPostInTeamChannel_when_allChannelIsFalse() {
        // given
        String idempotencyKey = UUID.randomUUID().toString();
        PostCreateRequest request = new PostCreateRequest(
                "팀 채널 게시글",
                false,
                null,
                null
        );

        // when
        postApplicationService.uploadPost(
                idempotencyKey, request, testUser.getId(), testTeam.getCode()
        );

        // then
        List<PostEntity> savedPosts = postJpaRepository.findAll();
        assertThat(savedPosts).hasSize(1);
        assertThat(savedPosts.get(0).getChannel()).isEqualTo(PostEntity.Channel.DOOSAN);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 게시글 업로드 시 UserNotFoundException을 발생시킨다")
    void should_throwUserNotFoundException_when_uploadPostWithNonExistentUser() {
        // given
        String idempotencyKey = UUID.randomUUID().toString();
        Long nonExistentUserId = 999L;
        PostCreateRequest request = new PostCreateRequest(
                "테스트 게시글",
                false,
                null,
                null
        );

        // when & then
        assertThatThrownBy(() -> postApplicationService.uploadPost(
                idempotencyKey, request, nonExistentUserId, testTeam.getCode()
        )).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("중복된 멱등성 키로 게시글 업로드 시 IdempotencyKeyException을 발생시킨다")
    void should_throwIdempotencyKeyException_when_uploadPostWithDuplicateIdempotencyKey() {
        // given
        String idempotencyKey = UUID.randomUUID().toString();
        PostCreateRequest request = new PostCreateRequest(
                "첫 번째 게시글",
                false,
                null,
                null
        );

        postApplicationService.uploadPost(
                idempotencyKey, request, testUser.getId(), testTeam.getCode()
        );

        PostCreateRequest duplicateRequest = new PostCreateRequest(
                "두 번째 게시글",
                false,
                null,
                null
        );

        // when & then
        assertThatThrownBy(() -> postApplicationService.uploadPost(
                idempotencyKey, duplicateRequest, testUser.getId(), testTeam.getCode()
        )).isInstanceOf(IdempotencyKeyException.class);
    }

    @Test
    @DisplayName("해시태그 없이 게시글 업로드 시 정상 처리된다")
    void should_uploadSuccessfully_when_uploadPostWithoutHashtags() {
        // given
        String idempotencyKey = UUID.randomUUID().toString();
        PostCreateRequest request = new PostCreateRequest(
                "해시태그 없는 게시글",
                false,
                null,
                null
        );

        // when
        PostUploadResponse response = postApplicationService.uploadPost(
                idempotencyKey, request, testUser.getId(), testTeam.getCode()
        );

        // then
        assertThat(response.isSuccess()).isTrue();

        List<PostEntity> savedPosts = postJpaRepository.findAll();
        assertThat(savedPosts).hasSize(1);

        List<PostHashtagEntity> savedHashtags = postHashtagRepository.findAll();
        assertThat(savedHashtags).isEmpty();
    }

    @Test
    @DisplayName("해시태그 목록 조회 시 모든 해시태그를 반환한다")
    void should_returnAllHashtags_when_getHashtags() {
        // when
        List<HashtagDto> hashtags = postApplicationService.getHashtags();

        // then
        assertThat(hashtags).hasSize(2);
        assertThat(hashtags).extracting(HashtagDto::getName)
                .containsExactlyInAnyOrder("야구", "응원");
    }

    @Test
    @DisplayName("해시태그가 없을 때 빈 리스트를 반환한다")
    void should_returnEmptyList_when_getHashtagsWithNoHashtags() {
        // given
        hashtagJpaRepository.deleteAll();
        hashtagJpaRepository.flush();

        // Clear cache
        if (cacheManager.getCache("hashtags") != null) {
            cacheManager.getCache("hashtags").clear();
        }

        // when
        List<HashtagDto> hashtags = postApplicationService.getHashtags();

        // then
        assertThat(hashtags).isEmpty();
    }
}
