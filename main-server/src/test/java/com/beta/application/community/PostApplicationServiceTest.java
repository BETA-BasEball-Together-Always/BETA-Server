package com.beta.application.community;

import com.beta.application.community.dto.ImageDto;
import com.beta.common.docker.TestContainer;
import com.beta.common.exception.idempotency.IdempotencyKeyException;
import com.beta.common.exception.image.ImageNotFoundException;
import com.beta.common.exception.post.PostAccessDeniedException;
import com.beta.common.exception.post.PostNotFoundException;
import com.beta.infra.community.entity.PostEntity;
import com.beta.infra.community.entity.PostImageEntity;
import com.beta.infra.community.entity.Status;
import com.beta.infra.community.gcs.GcsStorageClient;
import com.beta.infra.community.redis.CommunityRedisRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("PostApplicationService 통합 테스트")
class PostApplicationServiceTest extends TestContainer {

    @Autowired
    private PostApplicationService postApplicationService;

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private PostImageJpaRepository postImageJpaRepository;

    @Autowired
    private CommunityRedisRepository communityRedisRepository;

    @MockitoBean
    private GcsStorageClient gcsStorageClient;

    private PostEntity testPost;
    private Long testUserId = 1L;
    private String testIdempotencyKey;

    @BeforeEach
    void setUp() {
        testIdempotencyKey = UUID.randomUUID().toString();

        // 테스트용 게시글 생성
        testPost = PostEntity.builder()
                .userId(testUserId)
                .content("테스트 게시글")
                .channel(PostEntity.Channel.ALL)
                .build();
        testPost = postJpaRepository.save(testPost);
    }

    @AfterEach
    void tearDown() {
        postImageJpaRepository.deleteAll();
        postJpaRepository.deleteAll();
    }

    /**
     * JPEG 이미지 매직 넘버를 포함한 더미 바이트 생성
     */
    private byte[] createJpegBytes() {
        return new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46
        };
    }

    @Test
    @DisplayName("이미지 업로드 성공")
    void uploadImages_Success() throws Exception {
        // given
        MultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", createJpegBytes()
        );
        List<MultipartFile> files = List.of(file);

        ImageDto mockImageDto = ImageDto.builder()
                .imgUrl("https://storage.googleapis.com/test/image.jpg")
                .sort(1)
                .originName("test.jpg")
                .newName("unique-test.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .build();

        // GCS 호출을 Mock으로 처리
        when(gcsStorageClient.upload(any(MultipartFile.class), anyLong()))
                .thenReturn(mockImageDto);

        // when
        List<PostImagesResponse> response = postApplicationService.uploadImages(testIdempotencyKey, files, testUserId);

        // then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getImageUrl())
                .isEqualTo("https://storage.googleapis.com/test/image.jpg");
    }

    @Test
    @DisplayName("이미지 업로드 - 중복된 멱등성 키로 실패")
    void uploadImages_DuplicateIdempotencyKey() throws Exception {
        // given
        MultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", createJpegBytes()
        );
        List<MultipartFile> files = List.of(file);

        ImageDto mockImageDto = ImageDto.builder()
                .imgUrl("https://storage.googleapis.com/test/image.jpg")
                .sort(1)
                .originName("test.jpg")
                .newName("unique-test.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .build();

        when(gcsStorageClient.upload(any(MultipartFile.class), anyLong()))
                .thenReturn(mockImageDto);

        // 첫 번째 요청 성공
        postApplicationService.uploadImages(testIdempotencyKey, files, testUserId);

        // when & then - 같은 멱등성 키로 두 번째 요청 시 실패
        assertThatThrownBy(() ->
                postApplicationService.uploadImages(testIdempotencyKey, files, testUserId))
                .isInstanceOf(IdempotencyKeyException.class);
    }

    @Test
    @DisplayName("게시글에 이미지 추가 성공")
    @Transactional
    void insertImagesToPost_Success() throws Exception {
        // given
        MultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", createJpegBytes()
        );
        List<MultipartFile> files = List.of(file);

        ImageDto mockImageDto = ImageDto.builder()
                .imgUrl("https://storage.googleapis.com/test/image.jpg")
                .sort(1)
                .originName("test.jpg")
                .newName("unique-test.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .build();

        when(gcsStorageClient.upload(any(MultipartFile.class), anyLong()))
                .thenReturn(mockImageDto);

        // when
        List<PostImagesResponse> response = postApplicationService.insertImagesToPost(
                testIdempotencyKey, testPost.getId(), files, testUserId
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getImageUrl())
                .isEqualTo("https://storage.googleapis.com/test/image.jpg");
    }

    @Test
    @DisplayName("게시글에 이미지 추가 - 게시글이 존재하지 않으면 실패")
    void insertImagesToPost_PostNotFound() {
        // given
        Long nonExistentPostId = 999L;
        MultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", createJpegBytes()
        );
        List<MultipartFile> files = List.of(file);

        // when & then
        assertThatThrownBy(() ->
                postApplicationService.insertImagesToPost(
                        testIdempotencyKey, nonExistentPostId, files, testUserId
                ))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("게시글에 이미지 추가 - 권한이 없으면 실패")
    void insertImagesToPost_AccessDenied() {
        // given
        Long otherUserId = 999L;
        MultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", createJpegBytes()
        );
        List<MultipartFile> files = List.of(file);

        // when & then
        assertThatThrownBy(() ->
                postApplicationService.insertImagesToPost(
                        testIdempotencyKey, testPost.getId(), files, otherUserId
                ))
                .isInstanceOf(PostAccessDeniedException.class);
    }

    @Test
    @DisplayName("이미지 소프트 삭제 성공")
    @Transactional
    void softDeleteImages_Success() {
        // given
        PostImageEntity image1 = PostImageEntity.builder()
                .postId(testPost.getId())
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
                .imgUrl("https://storage.googleapis.com/test/image2.jpg")
                .originName("image2.jpg")
                .newName("unique-image2.jpg")
                .fileSize(2048L)
                .mimeType("image/jpeg")
                .sort(2)
                .status(Status.ACTIVE)
                .build();

        image1 = postImageJpaRepository.save(image1);
        image2 = postImageJpaRepository.save(image2);

        ImageDeleteRequest request = ImageDeleteRequest.builder()
                .imageIds(List.of(image1.getId(), image2.getId()))
                .build();

        // when
        ImageDeleteResponse response = postApplicationService.softDeleteImages(
                testPost.getId(), testIdempotencyKey, request, testUserId
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.getImages()).hasSize(2);

        // DB에서 삭제 상태 확인 - status가 MARKED_FOR_DELETION으로 변경됨
        List<PostImageEntity> allImages = postImageJpaRepository.findAll();
        List<PostImageEntity> deletedImages = allImages.stream()
                .filter(img -> img.getStatus() == Status.MARKED_FOR_DELETION)
                .toList();
        assertThat(deletedImages).hasSize(2);
    }

    @Test
    @DisplayName("이미지 소프트 삭제 - 존재하지 않는 이미지면 실패")
    void softDeleteImages_ImageNotFound() {
        // given
        ImageDeleteRequest request = ImageDeleteRequest.builder()
                .imageIds(List.of(999L, 1000L))
                .build();

        // when & then
        assertThatThrownBy(() ->
                postApplicationService.softDeleteImages(
                        testPost.getId(), testIdempotencyKey, request, testUserId
                ))
                .isInstanceOf(ImageNotFoundException.class);
    }
}
