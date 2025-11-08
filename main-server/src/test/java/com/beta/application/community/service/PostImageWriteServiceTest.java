package com.beta.application.community.service;

import com.beta.application.community.dto.ImageDto;
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
                .originName("test.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .build();

        when(gcsStorageClient.upload(dummy, 1L)).thenReturn(dummyDto);

        // when
        List<ImageDto> imageDtoList = postImageWriteService.uploadImages(images, 1L);
        // then
        assertThat(imageDtoList).hasSize(1);
        assertThat(imageDtoList.getFirst().getImgUrl()).isEqualTo("http://example.com/test.jpg");
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
                .originName("test.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .build();

        when(gcsStorageClient.upload(dummy,  1L)).thenThrow(IOException.class);

        // when & then
        assertThatThrownBy(() -> postImageWriteService.uploadImages(images, 1L))
                .isInstanceOf(ImageUploadFailedException.class)
                .hasMessage("이미지 업로드 중 오류가 발생했습니다");
    }

    @Test
    @DisplayName("이미지 메타데이터 저장 처리")
    void saveImagesMetadata_Success() {
        // given
        ImageDto dummyDto = ImageDto.builder()
                .imgUrl("http://example.com/test.jpg")
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
                .build();

        when(postImageJpaRepository.saveAll(anyList())).thenReturn(List.of(dummyEntity));
        // when
        List<ImageDto> list = postImageWriteService.saveImagesMetadata(images, null);
        // then
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getImgUrl()).isEqualTo("http://example.com/test.jpg");
        assertThat(list.getFirst().getSort()).isEqualTo(0);
        assertThat(list.getFirst().getOriginName()).isEqualTo("test.jpg");
    }

    @Test
    @DisplayName("이미지 정상 삭제 처리")
    void deleteImages_Success() throws Exception {
        ImageDto dummyDto = ImageDto.builder()
                .imgUrl("http://example.com/test.jpg")
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
                .build();

        PostImageEntity image2 = PostImageEntity.builder()
                .postId(postId)
                .imgUrl("http://example.com/image2.jpg")
                .originName("image2.jpg")
                .newName("unique-image2.jpg")
                .fileSize(2048L)
                .mimeType("image/jpeg")
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
}
