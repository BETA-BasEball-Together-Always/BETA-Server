package com.beta.unit.community;

import com.beta.application.community.dto.ImageDto;
import com.beta.application.community.service.PostImageWriteService;
import com.beta.common.exception.image.ImageUploadFailedException;
import com.beta.common.fixture.PostFixture;
import com.beta.domain.community.service.ImageValidationService;
import com.beta.infra.community.entity.PostImageEntity;
import com.beta.infra.community.entity.Status;
import com.beta.infra.community.gcs.GcsStorageClient;
import com.beta.infra.community.repository.ImageErrorJpaRepository;
import com.beta.infra.community.repository.PostImageJpaRepository;
import com.beta.infra.community.repository.PostJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostImageWriteService 단위 테스트")
class PostImageWriteServiceTest {

    @Mock
    private ImageValidationService imageValidationService;

    @Mock
    private GcsStorageClient gcsStorageClient;

    @Mock
    private PostImageJpaRepository postImageJpaRepository;

    @Mock
    private ImageErrorJpaRepository imageErrorJpaRepository;

    @Mock
    private PostJpaRepository postJpaRepository;

    @InjectMocks
    private PostImageWriteService postImageWriteService;

    @Test
    @DisplayName("이미지 업로드 성공 시 ImageDto 리스트를 반환한다")
    void should_returnImageDtoList_when_uploadImagesSuccessfully() throws Exception {
        // given
        Long userId = 1L;
        List<MultipartFile> images = PostFixture.createValidImages(2);

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

        doNothing().when(imageValidationService).validateImages(images);
        when(gcsStorageClient.upload(any(MultipartFile.class), eq(userId)))
                .thenReturn(mockImageDto1, mockImageDto2);

        // when
        List<ImageDto> result = postImageWriteService.uploadImages(images, userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getImgUrl()).contains("image1.jpg");
        assertThat(result.get(1).getImgUrl()).contains("image2.png");
        verify(imageValidationService).validateImages(images);
        verify(gcsStorageClient, times(2)).upload(any(MultipartFile.class), eq(userId));
    }

    @Test
    @DisplayName("이미지 업로드 중 오류 발생 시 업로드된 이미지를 롤백하고 ImageUploadFailedException을 발생시킨다")
    void should_rollbackAndThrowException_when_uploadImagesFails() throws Exception {
        // given
        Long userId = 1L;
        List<MultipartFile> images = PostFixture.createValidImages(2);

        ImageDto uploadedImage = ImageDto.builder()
                .imgUrl("https://storage.googleapis.com/test/uploaded.jpg")
                .newName("uploaded.jpg")
                .build();

        doNothing().when(imageValidationService).validateImages(images);
        when(gcsStorageClient.upload(any(MultipartFile.class), eq(userId)))
                .thenReturn(uploadedImage)
                .thenThrow(new RuntimeException("Upload failed"));
        when(gcsStorageClient.delete(anyString())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> postImageWriteService.uploadImages(images, userId))
                .isInstanceOf(ImageUploadFailedException.class)
                .hasMessageContaining("이미지 업로드 중 오류가 발생했습니다");

        verify(gcsStorageClient).delete("uploaded.jpg");
    }

    @Test
    @DisplayName("이미지 메타데이터 저장 시 PostImageEntity를 저장하고 ImageDto 리스트를 반환한다")
    void should_saveAndReturnImageDtoList_when_saveImagesMetadata() {
        // given
        Long postId = 1L;
        Long userId = 1L;
        List<ImageDto> uploadImages = List.of(
                ImageDto.builder()
                        .imgUrl("https://storage.googleapis.com/test/image1.jpg")
                        .sort(1)
                        .originName("test1.jpg")
                        .newName("unique1.jpg")
                        .mimeType("image/jpeg")
                        .fileSize(1024L)
                        .build()
        );

        PostImageEntity savedEntity = PostFixture.createPostImage(postId, 1);
        when(postImageJpaRepository.saveAll(anyList())).thenReturn(List.of(savedEntity));

        // when
        List<ImageDto> result = postImageWriteService.saveImagesMetadata(uploadImages, postId, userId);

        // then
        assertThat(result).isNotEmpty();
        verify(postImageJpaRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("이미지 소프트 삭제 시 상태를 MARKED_FOR_DELETION으로 변경한다")
    void should_markForDeletion_when_softDeleteImages() {
        // given
        Long postId = 1L;
        List<Long> imageIds = List.of(10L, 20L);

        PostImageEntity image1 = PostFixture.createPostImageWithStatus(postId, 1, Status.ACTIVE);
        PostImageEntity image2 = PostFixture.createPostImageWithStatus(postId, 2, Status.ACTIVE);

        when(postImageJpaRepository.findAllByIdInAndPostIdAndStatus(imageIds, postId, Status.ACTIVE))
                .thenReturn(List.of(image1, image2));
        when(postImageJpaRepository.saveAll(anyList())).thenReturn(List.of(image1, image2));

        // when
        List<ImageDto> result = postImageWriteService.softDeleteImages(postId, imageIds);

        // then
        assertThat(result).hasSize(2);
        verify(postImageJpaRepository).findAllByIdInAndPostIdAndStatus(imageIds, postId, Status.ACTIVE);
        verify(postImageJpaRepository).saveAll(anyList());
    }
}
