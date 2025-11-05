package com.beta.application.community.service;

import com.beta.application.community.dto.ImageDto;
import com.beta.common.exception.image.ImageOrderMismatchException;
import com.beta.common.exception.image.ImageUploadFailedException;
import com.beta.domain.community.service.ImageValidationService;
import com.beta.infra.community.entity.PostImageEntity;
import com.beta.infra.community.entity.Status;
import com.beta.infra.community.gcs.GcsStorageClient;
import com.beta.infra.community.repository.ImageErrorJpaRepository;
import com.beta.infra.community.repository.PostImageJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostImageWriteService 테스트")
class PostImageWriteServiceTest {

    @Mock
    private ImageValidationService imageValidationService;

    @Mock
    private GcsStorageClient gcsStorageClient;

    @Mock
    private PostImageJpaRepository postImageJpaRepository;

    @Mock
    private ImageErrorJpaRepository imageErrorJpaRepository;

    @InjectMocks
    private PostImageWriteService postImageWriteService;

    @Test
    @DisplayName("이미지 정상 업로드 처리")
    void uploadImages_Success() throws Exception {
        // given
        MultipartFile dummy = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "dummy".getBytes()
        );
        List<MultipartFile> images = List.of(dummy);
        ImageDto dummyDto = ImageDto.builder()
                .imgUrl("http://example.com/test.jpg")
                .sort(1)
                .originName("test.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .build();

        when(gcsStorageClient.upload(dummy, 1, 1L)).thenReturn(dummyDto);

        // when
        List<ImageDto> imageDtoList = postImageWriteService.uploadImages(images, 1L, 1);

        // then
        assertThat(imageDtoList).hasSize(1);
        assertThat(imageDtoList.getFirst().getImgUrl()).isEqualTo("http://example.com/test.jpg");
        assertThat(imageDtoList.getFirst().getSort()).isEqualTo(1);
        assertThat(imageDtoList.getFirst().getOriginName()).isEqualTo("test.jpg");
        assertThat(imageDtoList.getFirst().getMimeType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("이미지 업로드 시 IOException 발생 처리")
    void uploadImages_IOException() throws Exception {
        // given
        MultipartFile dummy = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "dummy".getBytes()
        );
        List<MultipartFile> images = List.of(dummy);
        ImageDto dummyDto = ImageDto.builder()
                .imgUrl("http://example.com/test.jpg")
                .sort(1)
                .originName("test.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .build();

        when(gcsStorageClient.upload(dummy, 1, 1L)).thenThrow(IOException.class);

        // when & then
        assertThatThrownBy(() -> postImageWriteService.uploadImages(images, 1L, 1))
                .isInstanceOf(ImageUploadFailedException.class)
                .hasMessage("이미지 업로드 중 오류가 발생했습니다");
    }

    @Test
    @DisplayName("이미지 메타데이터 저장 처리")
    void saveImagesMetadata_Success() {
        // given
        ImageDto dummyDto = ImageDto.builder()
                .imgUrl("http://example.com/test.jpg")
                .sort(1)
                .originName("test.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .build();
        List<ImageDto> images = List.of(dummyDto);
        PostImageEntity dummyEntity = PostImageEntity.builder()
                .imgUrl("http://example.com/test.jpg")
                .originName("test.jpg")
                .newName("unique-test.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .sort(1)
                .build();

        when(postImageJpaRepository.saveAll(anyList())).thenReturn(List.of(dummyEntity));
        // when
        List<ImageDto> list = postImageWriteService.saveImagesMetadata(images, null);
        // then
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getImgUrl()).isEqualTo("http://example.com/test.jpg");
        assertThat(list.getFirst().getSort()).isEqualTo(1);
        assertThat(list.getFirst().getOriginName()).isEqualTo("test.jpg");
    }

    @Test
    @DisplayName("이미지 정상 삭제 처리")
    void deleteImages_Success() throws Exception {
        ImageDto dummyDto = ImageDto.builder()
                .imgUrl("http://example.com/test.jpg")
                .sort(1)
                .newName("2025-11-04/test.jpg")
                .originName("test.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .build();
        List<ImageDto> images = List.of(dummyDto);
        when(gcsStorageClient.delete(images.getFirst().getNewName())).thenReturn(true);

        // when
        postImageWriteService.deleteImages(images,1L);

        // then
        verify(gcsStorageClient, times(1)).delete(dummyDto.getNewName());
    }

    @Test
    @DisplayName("이미지 삭제 시 IOException 발생 처리")
    void deleteImages_IOException() throws Exception {
        // given
        ImageDto dummyDto = ImageDto.builder()
                .imgUrl("http://example.com/test.jpg")
                .sort(1)
                .newName("2025-11-04/test.jpg")
                .originName("test.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .build();
        List<ImageDto> images = List.of(dummyDto);

        when(gcsStorageClient.delete(images.getFirst().getNewName())).thenThrow(IOException.class);

        // when & then
        postImageWriteService.deleteImages(images,1L);
        verify(imageErrorJpaRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("이미지 소프트 삭제 정상 처리")
    void softDeleteImages_Success() {
        // given
        Long postId = 1L;
        List<Long> imageIds = List.of(1L, 2L);

        PostImageEntity image1 = PostImageEntity.builder()
                .postId(postId)
                .imgUrl("http://example.com/image1.jpg")
                .originName("image1.jpg")
                .newName("unique-image1.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .sort(1)
                .build();

        PostImageEntity image2 = PostImageEntity.builder()
                .postId(postId)
                .imgUrl("http://example.com/image2.jpg")
                .originName("image2.jpg")
                .newName("unique-image2.jpg")
                .fileSize(2048L)
                .mimeType("image/jpeg")
                .sort(2)
                .build();

        List<PostImageEntity> images = List.of(image1, image2);

        when(postImageJpaRepository.findAllByIdInAndPostIdAndStatus(imageIds, postId, Status.ACTIVE))
                .thenReturn(images);
        when(postImageJpaRepository.saveAll(anyList())).thenReturn(images);

        // when
        List<ImageDto> result = postImageWriteService.softDeleteImages(postId, imageIds);

        // then
        assertThat(result).hasSize(2);
        verify(postImageJpaRepository, times(1)).findAllByIdInAndPostIdAndStatus(imageIds, postId, Status.ACTIVE);
        verify(postImageJpaRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("존재하지 않는 이미지 소프트 삭제 시 빈 리스트 반환")
    void softDeleteImages_NotFound() {
        // given
        Long postId = 1L;
        List<Long> imageIds = List.of(999L);

        when(postImageJpaRepository.findAllByIdInAndPostIdAndStatus(imageIds, postId, Status.ACTIVE))
                .thenReturn(List.of());
        when(postImageJpaRepository.saveAll(anyList())).thenReturn(List.of());

        // when
        List<ImageDto> result = postImageWriteService.softDeleteImages(postId, imageIds);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이미지 순서 변경 정상 처리")
    void updateImageOrder_Success() {
        // given
        Long postId = 1L;
        List<Long> imageOrders = List.of(3L, 1L, 2L);

        PostImageEntity image1 = PostImageEntity.builder()
                .postId(postId)
                .imgUrl("http://example.com/image1.jpg")
                .originName("image1.jpg")
                .newName("unique-image1.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .sort(1)
                .build();
        image1.testIdSet(1L);

        PostImageEntity image2 = PostImageEntity.builder()
                .postId(postId)
                .imgUrl("http://example.com/image2.jpg")
                .originName("image2.jpg")
                .newName("unique-image2.jpg")
                .fileSize(2048L)
                .mimeType("image/jpeg")
                .sort(2)
                .build();
        image2.testIdSet(2L);

        PostImageEntity image3 = PostImageEntity.builder()
                .postId(postId)
                .imgUrl("http://example.com/image3.jpg")
                .originName("image3.jpg")
                .newName("unique-image3.jpg")
                .fileSize(3072L)
                .mimeType("image/jpeg")
                .sort(3)
                .build();
        image3.testIdSet(3L);

        List<PostImageEntity> images = new ArrayList<>(Arrays.asList(image1, image2, image3));

        when(postImageJpaRepository.findAllByIdInAndPostIdAndStatusIn(imageOrders, postId, List.of(Status.PENDING, Status.ACTIVE)))
                .thenReturn(images);
        when(postImageJpaRepository.saveAll(anyList())).thenReturn(images);

        // when
        List<ImageDto> result = postImageWriteService.updateImageOrder(postId, imageOrders);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.getFirst().getPostImageId()).isEqualTo(1L);
        assertThat(result.getFirst().getSort()).isEqualTo(2L);
        assertThat(result.getLast().getPostImageId()).isEqualTo(3L);
        assertThat(result.getLast().getSort()).isEqualTo(1L);
        verify(postImageJpaRepository, times(1)).findAllByIdInAndPostIdAndStatusIn(imageOrders, postId, List.of(Status.PENDING, Status.ACTIVE));
        verify(postImageJpaRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("중복된 이미지 ID로 순서 변경 시 예외 발생")
    void updateImageOrder_DuplicateIds() {
        // given
        Long postId = 1L;
        List<Long> imageOrders = List.of(1L, 1L, 2L);

        PostImageEntity image1 = PostImageEntity.builder()
                .postId(postId)
                .imgUrl("http://example.com/image1.jpg")
                .originName("image1.jpg")
                .newName("unique-image1.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .sort(1)
                .build();
        image1.testIdSet(1L);

        PostImageEntity image2 = PostImageEntity.builder()
                .postId(postId)
                .imgUrl("http://example.com/image2.jpg")
                .originName("image2.jpg")
                .newName("unique-image2.jpg")
                .fileSize(2048L)
                .mimeType("image/jpeg")
                .sort(2)
                .build();
        image2.testIdSet(2L);

        List<PostImageEntity> images = List.of(image1, image2);

        when(postImageJpaRepository.findAllByIdInAndPostIdAndStatusIn(imageOrders, postId, List.of(Status.PENDING, Status.ACTIVE)))
                .thenReturn(images);

        // when & then
        assertThatThrownBy(() -> postImageWriteService.updateImageOrder(postId, imageOrders))
                .isInstanceOf(ImageOrderMismatchException.class);
    }

    @Test
    @DisplayName("DB 조회 결과와 요청 개수 불일치 시 예외 발생")
    void updateImageOrder_CountMismatch() {
        // given
        Long postId = 1L;
        List<Long> imageOrders = List.of(1L, 2L, 3L);

        PostImageEntity image1 = PostImageEntity.builder()
                .postId(postId)
                .imgUrl("http://example.com/image1.jpg")
                .originName("image1.jpg")
                .newName("unique-image1.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .sort(1)
                .build();

        List<PostImageEntity> images = List.of(image1);

        when(postImageJpaRepository.findAllByIdInAndPostIdAndStatusIn(imageOrders, postId, List.of(Status.PENDING, Status.ACTIVE)))
                .thenReturn(images);

        // when & then
        assertThatThrownBy(() -> postImageWriteService.updateImageOrder(postId, imageOrders))
                .isInstanceOf(ImageOrderMismatchException.class);
    }
}
