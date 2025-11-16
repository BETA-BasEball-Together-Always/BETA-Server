package com.beta.unit.community;

import com.beta.application.community.dto.ImageDto;
import com.beta.application.community.service.PostImageWriteService;
import com.beta.common.exception.image.ImageUploadFailedException;
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
import org.springframework.mock.web.MockMultipartFile;
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
    @DisplayName("이미지 업로드 - 정상 동작")
    void uploadImages_success() throws Exception {
        // given
        MultipartFile file = new MockMultipartFile("test", "test.jpg", "image/jpeg", "test".getBytes());
        ImageDto imageDto = ImageDto.builder()
                .imgUrl("https://test.com/image.jpg")
                .newName("image.jpg")
                .build();

        doNothing().when(imageValidationService).validateImages(any());
        when(gcsStorageClient.upload(any(), anyLong())).thenReturn(imageDto);

        // when
        List<ImageDto> result = postImageWriteService.uploadImages(List.of(file), 1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getImgUrl()).isEqualTo("https://test.com/image.jpg");
    }

    @Test
    @DisplayName("이미지 업로드 - 실패시 GCS 롤백 및 예외 발생")
    void uploadImages_throwsException_and_rollbacksGCS() throws Exception {
        // given
        MultipartFile file1 = new MockMultipartFile("test1", "test1.jpg", "image/jpeg", "test1".getBytes());
        MultipartFile file2 = new MockMultipartFile("test2", "test2.jpg", "image/jpeg", "test2".getBytes());

        ImageDto uploadedDto = ImageDto.builder()
                .imgUrl("https://test.com/uploaded.jpg")
                .newName("uploaded.jpg")
                .build();

        doNothing().when(imageValidationService).validateImages(any());
        when(gcsStorageClient.upload(any(), anyLong()))
                .thenReturn(uploadedDto)
                .thenThrow(new RuntimeException("Upload failed"));
        when(gcsStorageClient.delete(anyString())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> postImageWriteService.uploadImages(List.of(file1, file2), 1L))
                .isInstanceOf(ImageUploadFailedException.class);

        // 첫 번째 이미지는 롤백됨
        verify(gcsStorageClient).delete("uploaded.jpg");
    }

    @Test
    @DisplayName("이미지 메타데이터 저장 - 정상 동작")
    void saveImagesMetadata_success() {
        // given
        ImageDto dto = ImageDto.builder()
                .imgUrl("https://test.com/image.jpg")
                .originName("test.jpg")
                .newName("new.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .sort(1)
                .build();

        PostImageEntity entity = mock(PostImageEntity.class);
        when(postImageJpaRepository.saveAll(any())).thenReturn(List.of(entity));

        // when
        List<ImageDto> result = postImageWriteService.saveImagesMetadata(List.of(dto), 1L, 1L);

        // then
        assertThat(result).isNotEmpty();
        verify(postImageJpaRepository).saveAll(any());
    }

    @Test
    @DisplayName("이미지 Soft Delete - 정상 동작 (MARKED_FOR_DELETION)")
    void softDeleteImages_success() {
        // given
        PostImageEntity image = mock(PostImageEntity.class);
        when(postImageJpaRepository.findAllByIdInAndPostIdAndStatus(any(), anyLong(), eq(Status.ACTIVE)))
                .thenReturn(List.of(image));
        when(postImageJpaRepository.saveAll(any())).thenReturn(List.of(image));

        // when
        List<ImageDto> result = postImageWriteService.softDeleteImages(1L, List.of(1L));

        // then
        assertThat(result).hasSize(1);
        verify(image).markForDeletion();
    }
}
