package com.beta.integration.community;

import com.beta.application.community.PostImageApplicationService;
import com.beta.application.community.dto.ImageDto;
import com.beta.common.docker.TestContainer;
import com.beta.common.exception.idempotency.IdempotencyKeyException;
import com.beta.common.exception.image.ImageNotFoundException;
import com.beta.common.exception.post.PostAccessDeniedException;
import com.beta.common.exception.post.PostNotFoundException;
import com.beta.common.fixture.PostFixture;
import com.beta.common.fixture.TeamFixture;
import com.beta.common.fixture.UserFixture;
import com.beta.infra.auth.entity.UserEntity;
import com.beta.infra.auth.repository.UserJpaRepository;
import com.beta.infra.common.entity.BaseballTeamEntity;
import com.beta.infra.common.repository.BaseballTeamRepository;
import com.beta.infra.community.entity.PostEntity;
import com.beta.infra.community.entity.PostImageEntity;
import com.beta.infra.community.entity.Status;
import com.beta.infra.community.gcs.GcsStorageClient;
import com.beta.infra.community.repository.PostImageJpaRepository;
import com.beta.infra.community.repository.PostJpaRepository;
import com.beta.presentation.community.request.ImageDeleteRequest;
import com.beta.presentation.community.response.ImageDeleteResponse;
import com.beta.presentation.community.response.PostImagesResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@DisplayName("PostImageApplicationService 통합 테스트")
class PostImageApplicationServiceIntegrationTest extends TestContainer {

    @Autowired
    private PostImageApplicationService postImageApplicationService;

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private PostImageJpaRepository postImageJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BaseballTeamRepository baseballTeamRepository;

    @MockitoBean
    private GcsStorageClient gcsStorageClient;

    private BaseballTeamEntity testTeam;
    private UserEntity testUser;
    private PostEntity testPost;

    @BeforeEach
    void setUp() {
        testTeam = TeamFixture.createDoosan();
        baseballTeamRepository.save(testTeam);

        testUser = UserFixture.createActiveUser("test_social_id", "테스트유저", testTeam);
        userJpaRepository.save(testUser);

        testPost = PostFixture.createPost(testUser.getId(), "테스트 게시글", PostEntity.Channel.ALL);
        postJpaRepository.save(testPost);
    }

    @AfterEach
    void tearDown() {
        postImageJpaRepository.deleteAll();
        postJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        baseballTeamRepository.deleteAll();
    }

    @Test
    @DisplayName("이미지 업로드 시 GCS에 업로드하고 DB에 PENDING 상태로 저장한다")
    void should_uploadToGcsAndSaveAsPending_when_uploadImages() throws Exception {
        // given
        List<MultipartFile> files = PostFixture.createValidImages(2);

        ImageDto mockImageDto1 = ImageDto.builder()
                .imgUrl("https://storage.googleapis.com/test/image1.jpg")
                .sort(1)
                .originName("test1.jpg")
                .newName("unique1.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .build();

        ImageDto mockImageDto2 = ImageDto.builder()
                .imgUrl("https://storage.googleapis.com/test/image2.png")
                .sort(2)
                .originName("test2.png")
                .newName("unique2.png")
                .mimeType("image/png")
                .fileSize(2048L)
                .build();

        when(gcsStorageClient.upload(any(MultipartFile.class), eq(testUser.getId())))
                .thenReturn(mockImageDto1, mockImageDto2);

        // when
        List<PostImagesResponse> result = postImageApplicationService.uploadImages(
                files, testUser.getId()
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getImageUrl()).contains("image1.jpg");
        assertThat(result.get(1).getImageUrl()).contains("image2.png");

        // DB 검증: postId가 null이고 status가 PENDING이어야 함
        List<PostImageEntity> savedImages = postImageJpaRepository.findAll();
        assertThat(savedImages).hasSize(2);
        assertThat(savedImages).allMatch(img -> img.getPostId() == null);
        assertThat(savedImages).allMatch(img -> img.getStatus() == Status.PENDING);
    }

    @Test
    @DisplayName("게시글에 이미지 추가 시 postId를 연결하고 ACTIVE 상태로 변경한다")
    void should_linkPostIdAndSetActive_when_insertImagesToPost() throws Exception {
        // given
        List<MultipartFile> files = PostFixture.createValidImages(1);

        ImageDto mockImageDto = ImageDto.builder()
                .imgUrl("https://storage.googleapis.com/test/image.jpg")
                .sort(1)
                .originName("test.jpg")
                .newName("unique.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .build();

        when(gcsStorageClient.upload(any(MultipartFile.class), eq(testUser.getId())))
                .thenReturn(mockImageDto);

        // when
        List<PostImagesResponse> result = postImageApplicationService.insertImagesToPost(
                testPost.getId(), files, testUser.getId()
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getImageUrl()).contains("image.jpg");

        // DB 검증: postId가 연결되고 status가 ACTIVE여야 함
        List<PostImageEntity> savedImages = postImageJpaRepository.findAll();
        assertThat(savedImages).hasSize(1);
        assertThat(savedImages.get(0).getPostId()).isEqualTo(testPost.getId());
        assertThat(savedImages.get(0).getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 이미지 추가 시 PostNotFoundException을 발생시킨다")
    void should_throwPostNotFoundException_when_insertImagesToNonExistentPost() {
        // given
        Long nonExistentPostId = 999L;
        List<MultipartFile> files = PostFixture.createValidImages(1);

        // when & then
        assertThatThrownBy(() -> postImageApplicationService.insertImagesToPost(
                nonExistentPostId, files, testUser.getId()
        )).isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("다른 사용자의 게시글에 이미지 추가 시 PostAccessDeniedException을 발생시킨다")
    void should_throwPostAccessDeniedException_when_insertImagesToOthersPost() {
        // given
        Long otherUserId = 999L;
        List<MultipartFile> files = PostFixture.createValidImages(1);

        // when & then
        assertThatThrownBy(() -> postImageApplicationService.insertImagesToPost(
                testPost.getId(), files, otherUserId
        )).isInstanceOf(PostAccessDeniedException.class);
    }

    @Test
    @DisplayName("이미지 소프트 삭제 시 status를 MARKED_FOR_DELETION으로 변경한다")
    void should_markForDeletion_when_softDeleteImages() {
        // given
        PostImageEntity image1 = PostImageEntity.builder()
                .postId(testPost.getId())
                .userId(testUser.getId())
                .imgUrl("https://storage.googleapis.com/test/image1.jpg")
                .originName("image1.jpg")
                .newName("unique-image1.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .sort(1)
                .status(Status.ACTIVE)
                .build();

        PostImageEntity image2 = PostImageEntity.builder()
                .postId(testPost.getId())
                .userId(testUser.getId())
                .imgUrl("https://storage.googleapis.com/test/image2.jpg")
                .originName("image2.jpg")
                .newName("unique-image2.jpg")
                .fileSize(2048L)
                .mimeType("image/jpeg")
                .sort(2)
                .status(Status.ACTIVE)
                .build();

        postImageJpaRepository.saveAll(List.of(image1, image2));

        ImageDeleteRequest request = new ImageDeleteRequest(
                List.of(image1.getId(), image2.getId())
        );

        // when
        ImageDeleteResponse result = postImageApplicationService.softDeleteImages(
                testPost.getId(), request, testUser.getId()
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getImages()).hasSize(2);

        // DB 검증: status가 MARKED_FOR_DELETION으로 변경됨
        List<PostImageEntity> deletedImages = postImageJpaRepository.findAll().stream()
                .filter(img -> img.getStatus() == Status.MARKED_FOR_DELETION)
                .toList();
        assertThat(deletedImages).hasSize(2);
    }

    @Test
    @DisplayName("존재하지 않는 이미지 소프트 삭제 시 ImageNotFoundException을 발생시킨다")
    void should_throwImageNotFoundException_when_softDeleteNonExistentImages() {
        // given
        ImageDeleteRequest request = new ImageDeleteRequest(
                List.of(999L, 1000L)
        );

        // when & then
        assertThatThrownBy(() -> postImageApplicationService.softDeleteImages(
                testPost.getId(), request, testUser.getId()
        )).isInstanceOf(ImageNotFoundException.class);
    }

    // TODO: Rewrite this test for AOP-based idempotency
    // @Test
    // @DisplayName("중복된 멱등성 키로 이미지 업로드 시 IdempotencyKeyException을 발생시킨다")
    // void should_throwIdempotencyKeyException_when_uploadImagesWithDuplicateKey() throws Exception {
    //     // given
    //     List<MultipartFile> files = PostFixture.createValidImages(1);
    //
    //     ImageDto mockImageDto = ImageDto.builder()
    //             .imgUrl("https://storage.googleapis.com/test/image.jpg")
    //             .sort(1)
    //             .originName("test.jpg")
    //             .newName("unique.jpg")
    //             .mimeType("image/jpeg")
    //             .fileSize(1024L)
    //             .build();
    //
    //     when(gcsStorageClient.upload(any(MultipartFile.class), anyLong()))
    //             .thenReturn(mockImageDto);
    //
    //     // 첫 번째 업로드 성공
    //     postImageApplicationService.uploadImages(files, testUser.getId());
    //
    //     // when & then - 같은 멱등성 키로 두 번째 요청
    //     assertThatThrownBy(() -> postImageApplicationService.uploadImages(
    //             files, testUser.getId()
    //     )).isInstanceOf(IdempotencyKeyException.class);
    // }
}
