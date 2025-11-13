package com.beta.integration.community;

import com.beta.application.community.PostApplicationService;
import com.beta.application.community.dto.HashtagDto;
import com.beta.common.docker.TestContainer;
import com.beta.common.exception.auth.UserNotFoundException;
import com.beta.common.exception.idempotency.IdempotencyKeyException;
import com.beta.common.exception.post.HashtagCountExceededException;
import com.beta.common.exception.post.PostAccessDeniedException;
import com.beta.common.exception.post.PostNotFoundException;
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
import com.beta.presentation.community.request.PostContentUpdateRequest;
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
                List.of("야구", "응원")
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

        // DB 검증: 기존 해시태그의 usageCount가 증가했는지
        List<HashtagEntity> hashtags = hashtagJpaRepository.findAll();
        assertThat(hashtags).hasSize(2);
        assertThat(hashtags).allMatch(h -> h.getUsageCount() == 1L);
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

    @Test
    @DisplayName("게시글 내용 수정 시 내용이 업데이트된다")
    void should_updateContent_when_updatePostContent() {
        // given
        // 1. 먼저 게시글 생성
        String uploadKey = UUID.randomUUID().toString();
        PostCreateRequest createRequest = new PostCreateRequest(
                "원본 게시글 내용",
                false,
                null,
                List.of("야구")
        );
        postApplicationService.uploadPost(uploadKey, createRequest, testUser.getId(), testTeam.getCode());

        PostEntity savedPost = postJpaRepository.findAll().get(0);
        Long postId = savedPost.getId();

        // 2. 게시글 수정
        String updateKey = UUID.randomUUID().toString();
        PostContentUpdateRequest updateRequest = new PostContentUpdateRequest(
                "수정된 게시글 내용",
                null,
                null
        );

        // when
        PostUploadResponse response = postApplicationService.updatePostContent(
                postId, updateKey, updateRequest, testUser.getId()
        );

        // then
        assertThat(response.isSuccess()).isTrue();

        PostEntity updatedPost = postJpaRepository.findById(postId).orElseThrow();
        assertThat(updatedPost.getContent()).isEqualTo("수정된 게시글 내용");
    }

    @Test
    @DisplayName("게시글 수정 시 해시태그를 추가할 수 있다")
    void should_addHashtags_when_updatePostContentWithNewHashtags() {
        // given
        // 1. 해시태그 없이 게시글 생성
        String uploadKey = UUID.randomUUID().toString();
        PostCreateRequest createRequest = new PostCreateRequest(
                "원본 게시글 내용",
                false,
                null,
                null
        );
        postApplicationService.uploadPost(uploadKey, createRequest, testUser.getId(), testTeam.getCode());

        PostEntity savedPost = postJpaRepository.findAll().get(0);
        Long postId = savedPost.getId();

        // 2. 해시태그 추가하며 수정
        String updateKey = UUID.randomUUID().toString();
        PostContentUpdateRequest updateRequest = new PostContentUpdateRequest(
                "수정된 게시글 내용",
                List.of("야구", "응원"),
                null
        );

        // when
        postApplicationService.updatePostContent(postId, updateKey, updateRequest, testUser.getId());

        // then
        List<PostHashtagEntity> hashtags = postHashtagRepository.findByPostId(postId);
        assertThat(hashtags).hasSize(2);
        assertThat(hashtags).extracting(PostHashtagEntity::getHashtagId)
                .containsExactlyInAnyOrder(hashtag1.getId(), hashtag2.getId());
    }

    @Test
    @DisplayName("게시글 수정 시 해시태그를 삭제할 수 있다")
    void should_deleteHashtags_when_updatePostContentWithDeleteHashtags() {
        // given
        // 1. 해시태그 2개와 함께 게시글 생성
        String uploadKey = UUID.randomUUID().toString();
        PostCreateRequest createRequest = new PostCreateRequest(
                "원본 게시글 내용",
                false,
                null,
                List.of("야구", "응원")
        );
        postApplicationService.uploadPost(uploadKey, createRequest, testUser.getId(), testTeam.getCode());

        PostEntity savedPost = postJpaRepository.findAll().get(0);
        Long postId = savedPost.getId();

        List<PostHashtagEntity> originalHashtags = postHashtagRepository.findByPostId(postId);
        Long hashtagToDeleteId = originalHashtags.get(0).getId();

        // 2. 해시태그 1개 삭제하며 수정
        String updateKey = UUID.randomUUID().toString();
        PostContentUpdateRequest updateRequest = new PostContentUpdateRequest(
                "수정된 게시글 내용",
                null,
                List.of(hashtagToDeleteId)
        );

        // when
        postApplicationService.updatePostContent(postId, updateKey, updateRequest, testUser.getId());

        // then
        List<PostHashtagEntity> remainingHashtags = postHashtagRepository.findByPostId(postId);
        assertThat(remainingHashtags).hasSize(1);
    }

    @Test
    @DisplayName("게시글 수정 시 해시태그를 삭제하고 추가할 수 있다")
    void should_deleteAndAddHashtags_when_updatePostContent() {
        // given
        // 1. 해시태그 1개와 함께 게시글 생성
        String uploadKey = UUID.randomUUID().toString();
        PostCreateRequest createRequest = new PostCreateRequest(
                "원본 게시글 내용",
                false,
                null,
                List.of("야구")
        );
        postApplicationService.uploadPost(uploadKey, createRequest, testUser.getId(), testTeam.getCode());

        PostEntity savedPost = postJpaRepository.findAll().get(0);
        Long postId = savedPost.getId();

        List<PostHashtagEntity> originalHashtags = postHashtagRepository.findByPostId(postId);
        Long hashtagToDeleteId = originalHashtags.get(0).getId();

        // 2. 기존 해시태그 삭제하고 새로운 해시태그 추가
        String updateKey = UUID.randomUUID().toString();
        PostContentUpdateRequest updateRequest = new PostContentUpdateRequest(
                "수정된 게시글 내용",
                List.of("응원"),
                List.of(hashtagToDeleteId)
        );

        // when
        postApplicationService.updatePostContent(postId, updateKey, updateRequest, testUser.getId());

        // then
        List<PostHashtagEntity> hashtags = postHashtagRepository.findByPostId(postId);
        assertThat(hashtags).hasSize(1);
        assertThat(hashtags.get(0).getHashtagId()).isEqualTo(hashtag2.getId());
    }

    @Test
    @DisplayName("다른 사용자의 게시글 수정 시 PostAccessDeniedException을 발생시킨다")
    void should_throwPostAccessDeniedException_when_updateOtherUserPost() {
        // given
        // 1. testUser가 게시글 생성
        String uploadKey = UUID.randomUUID().toString();
        PostCreateRequest createRequest = new PostCreateRequest(
                "원본 게시글 내용",
                false,
                null,
                null
        );
        postApplicationService.uploadPost(uploadKey, createRequest, testUser.getId(), testTeam.getCode());

        PostEntity savedPost = postJpaRepository.findAll().get(0);
        Long postId = savedPost.getId();

        // 2. 다른 사용자 생성
        UserEntity otherUser = UserFixture.createActiveUser("other_social_id", "다른유저", testTeam);
        userJpaRepository.save(otherUser);

        // 3. 다른 사용자가 수정 시도
        String updateKey = UUID.randomUUID().toString();
        PostContentUpdateRequest updateRequest = new PostContentUpdateRequest(
                "수정된 게시글 내용",
                null,
                null
        );

        // when & then
        assertThatThrownBy(() -> postApplicationService.updatePostContent(
                postId, updateKey, updateRequest, otherUser.getId()
        )).isInstanceOf(PostAccessDeniedException.class);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 시 PostNotFoundException을 발생시킨다")
    void should_throwPostNotFoundException_when_updateNonExistentPost() {
        // given
        Long nonExistentPostId = 999L;
        String updateKey = UUID.randomUUID().toString();
        PostContentUpdateRequest updateRequest = new PostContentUpdateRequest(
                "수정된 게시글 내용",
                null,
                null
        );

        // when & then
        assertThatThrownBy(() -> postApplicationService.updatePostContent(
                nonExistentPostId, updateKey, updateRequest, testUser.getId()
        )).isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("게시글 수정 시 해시태그가 10개를 초과하면 HashtagCountExceededException을 발생시킨다")
    void should_throwHashtagCountExceededException_when_updatePostWithTooManyHashtags() {
        // given
        // 1. 추가 해시태그 생성 (총 10개 이상 필요)
        List<HashtagEntity> additionalHashtags = List.of(
                PostFixture.createHashtag("태그3"),
                PostFixture.createHashtag("태그4"),
                PostFixture.createHashtag("태그5"),
                PostFixture.createHashtag("태그6"),
                PostFixture.createHashtag("태그7"),
                PostFixture.createHashtag("태그8"),
                PostFixture.createHashtag("태그9"),
                PostFixture.createHashtag("태그10")
        );
        hashtagJpaRepository.saveAll(additionalHashtags);

        // 2. 5개 해시태그와 함께 게시글 생성
        String uploadKey = UUID.randomUUID().toString();
        PostCreateRequest createRequest = new PostCreateRequest(
                "원본 게시글 내용",
                false,
                null,
                List.of(
                        hashtag1.getId(),
                        hashtag2.getId(),
                        additionalHashtags.get(0).getId(),
                        additionalHashtags.get(1).getId(),
                        additionalHashtags.get(2).getId()
                )
        );
        postApplicationService.uploadPost(uploadKey, createRequest, testUser.getId(), testTeam.getCode());

        PostEntity savedPost = postJpaRepository.findAll().get(0);
        Long postId = savedPost.getId();

        // 3. 6개를 더 추가하려고 시도 (총 11개)
        String updateKey = UUID.randomUUID().toString();
        PostContentUpdateRequest updateRequest = new PostContentUpdateRequest(
                "수정된 게시글 내용",
                List.of(
                        additionalHashtags.get(3).getId(),
                        additionalHashtags.get(4).getId(),
                        additionalHashtags.get(5).getId(),
                        additionalHashtags.get(6).getId(),
                        additionalHashtags.get(7).getId(),
                        additionalHashtags.get(0).getId()
                ),
                null
        );

        // when & then
        assertThatThrownBy(() -> postApplicationService.updatePostContent(
                postId, updateKey, updateRequest, testUser.getId()
        )).isInstanceOf(HashtagCountExceededException.class);
    }

    @Test
    @DisplayName("중복된 멱등성 키로 게시글 수정 시 IdempotencyKeyException을 발생시킨다")
    void should_throwIdempotencyKeyException_when_updatePostWithDuplicateIdempotencyKey() {
        // given
        // 1. 게시글 생성
        String uploadKey = UUID.randomUUID().toString();
        PostCreateRequest createRequest = new PostCreateRequest(
                "원본 게시글 내용",
                false,
                null,
                null
        );
        postApplicationService.uploadPost(uploadKey, createRequest, testUser.getId(), testTeam.getCode());

        PostEntity savedPost = postJpaRepository.findAll().get(0);
        Long postId = savedPost.getId();

        // 2. 첫 번째 수정
        String updateKey = UUID.randomUUID().toString();
        PostContentUpdateRequest updateRequest1 = new PostContentUpdateRequest(
                "첫 번째 수정",
                null,
                null
        );
        postApplicationService.updatePostContent(postId, updateKey, updateRequest1, testUser.getId());

        // 3. 같은 멱등성 키로 다시 수정 시도
        PostContentUpdateRequest updateRequest2 = new PostContentUpdateRequest(
                "두 번째 수정",
                null,
                null
        );

        // when & then
        assertThatThrownBy(() -> postApplicationService.updatePostContent(
                postId, updateKey, updateRequest2, testUser.getId()
        )).isInstanceOf(IdempotencyKeyException.class);
    }

    @Test
    @DisplayName("게시글 삭제 시 소프트 삭제가 수행된다")
    void should_softDeletePost_when_deletePost() {
        // given
        // 1. 게시글 생성
        String uploadKey = UUID.randomUUID().toString();
        PostCreateRequest createRequest = new PostCreateRequest(
                "삭제될 게시글",
                false,
                null,
                null
        );
        postApplicationService.uploadPost(uploadKey, createRequest, testUser.getId(), testTeam.getCode());

        PostEntity savedPost = postJpaRepository.findAll().get(0);
        Long postId = savedPost.getId();

        // 2. 게시글 삭제
        String deleteKey = UUID.randomUUID().toString();

        // when
        postApplicationService.deletePost(postId, testUser.getId(), deleteKey);

        // then
        PostEntity deletedPost = postJpaRepository.findById(postId).orElseThrow();
        assertThat(deletedPost.getStatus()).isEqualTo(Status.DELETED);
        assertThat(deletedPost.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("게시글 삭제 시 연관된 이미지도 소프트 삭제된다")
    void should_softDeleteImages_when_deletePost() {
        // given
        // 1. 이미지와 함께 게시글 생성
        PostImageEntity image1 = PostImageEntity.builder()
                .postId(null)
                .userId(testUser.getId())
                .imgUrl("https://storage.googleapis.com/test/image1.jpg")
                .originName("image1.jpg")
                .newName("unique-image1.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .sort(0)
                .status(Status.PENDING)
                .build();
        postImageJpaRepository.save(image1);

        String uploadKey = UUID.randomUUID().toString();
        PostCreateRequest createRequest = new PostCreateRequest(
                "이미지가 있는 게시글",
                false,
                List.of(new PostCreateRequest.Image(image1.getId(), 1)),
                null
        );
        postApplicationService.uploadPost(uploadKey, createRequest, testUser.getId(), testTeam.getCode());

        PostEntity savedPost = postJpaRepository.findAll().get(0);
        Long postId = savedPost.getId();

        // 2. 게시글 삭제
        String deleteKey = UUID.randomUUID().toString();

        // when
        postApplicationService.deletePost(postId, testUser.getId(), deleteKey);

        // then
        PostEntity deletedPost = postJpaRepository.findById(postId).orElseThrow();
        assertThat(deletedPost.getStatus()).isEqualTo(Status.DELETED);

        PostImageEntity deletedImage = postImageJpaRepository.findById(image1.getId()).orElseThrow();
        assertThat(deletedImage.getStatus()).isEqualTo(Status.DELETED);
    }

    @Test
    @DisplayName("게시글 삭제 시 해시태그는 유지된다")
    void should_keepHashtags_when_deletePost() {
        // given
        // 1. 해시태그와 함께 게시글 생성
        String uploadKey = UUID.randomUUID().toString();
        PostCreateRequest createRequest = new PostCreateRequest(
                "해시태그가 있는 게시글",
                false,
                null,
                List.of("야구", "응원")
        );
        postApplicationService.uploadPost(uploadKey, createRequest, testUser.getId(), testTeam.getCode());

        PostEntity savedPost = postJpaRepository.findAll().get(0);
        Long postId = savedPost.getId();

        // 2. 삭제 전 해시태그 개수 확인
        List<PostHashtagEntity> hashtagsBeforeDelete = postHashtagRepository.findByPostId(postId);
        assertThat(hashtagsBeforeDelete).hasSize(2);

        // 3. 게시글 삭제
        String deleteKey = UUID.randomUUID().toString();

        // when
        postApplicationService.deletePost(postId, testUser.getId(), deleteKey);

        // then
        PostEntity deletedPost = postJpaRepository.findById(postId).orElseThrow();
        assertThat(deletedPost.getStatus()).isEqualTo(Status.DELETED);

        // 해시태그는 여전히 유지됨
        List<PostHashtagEntity> hashtagsAfterDelete = postHashtagRepository.findByPostId(postId);
        assertThat(hashtagsAfterDelete).hasSize(2);
        assertThat(hashtagsAfterDelete).extracting(PostHashtagEntity::getHashtagId)
                .containsExactlyInAnyOrder(hashtag1.getId(), hashtag2.getId());
    }

    @Test
    @DisplayName("다른 사용자의 게시글 삭제 시 PostAccessDeniedException을 발생시킨다")
    void should_throwPostAccessDeniedException_when_deleteOtherUserPost() {
        // given
        // 1. testUser가 게시글 생성
        String uploadKey = UUID.randomUUID().toString();
        PostCreateRequest createRequest = new PostCreateRequest(
                "원본 게시글",
                false,
                null,
                null
        );
        postApplicationService.uploadPost(uploadKey, createRequest, testUser.getId(), testTeam.getCode());

        PostEntity savedPost = postJpaRepository.findAll().get(0);
        Long postId = savedPost.getId();

        // 2. 다른 사용자 생성
        UserEntity otherUser = UserFixture.createActiveUser("other_social_id", "다른유저", testTeam);
        userJpaRepository.save(otherUser);

        // 3. 다른 사용자가 삭제 시도
        String deleteKey = UUID.randomUUID().toString();

        // when & then
        assertThatThrownBy(() -> postApplicationService.deletePost(
                postId, otherUser.getId(), deleteKey
        )).isInstanceOf(PostAccessDeniedException.class);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제 시 PostNotFoundException을 발생시킨다")
    void should_throwPostNotFoundException_when_deleteNonExistentPost() {
        // given
        Long nonExistentPostId = 999L;
        String deleteKey = UUID.randomUUID().toString();

        // when & then
        assertThatThrownBy(() -> postApplicationService.deletePost(
                nonExistentPostId, testUser.getId(), deleteKey
        )).isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("중복된 멱등성 키로 게시글 삭제 시 IdempotencyKeyException을 발생시킨다")
    void should_throwIdempotencyKeyException_when_deletePostWithDuplicateIdempotencyKey() {
        // given
        // 1. 게시글 생성
        String uploadKey = UUID.randomUUID().toString();
        PostCreateRequest createRequest = new PostCreateRequest(
                "원본 게시글",
                false,
                null,
                null
        );
        postApplicationService.uploadPost(uploadKey, createRequest, testUser.getId(), testTeam.getCode());

        PostEntity savedPost = postJpaRepository.findAll().get(0);
        Long postId = savedPost.getId();

        // 2. 첫 번째 삭제
        String deleteKey = UUID.randomUUID().toString();
        postApplicationService.deletePost(postId, testUser.getId(), deleteKey);

        // 3. 같은 멱등성 키로 다시 삭제 시도
        // when & then
        assertThatThrownBy(() -> postApplicationService.deletePost(
                postId, testUser.getId(), deleteKey
        )).isInstanceOf(IdempotencyKeyException.class);
    }
}
