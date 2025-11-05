package com.beta.application.community.service;

import com.beta.common.exception.image.ImageCountExceededException;
import com.beta.infra.community.entity.PostImageEntity;
import com.beta.infra.community.entity.Status;
import com.beta.infra.community.repository.PostImageJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostImageReadService 테스트")
class PostImageReadServiceTest {

    @Mock
    private PostImageJpaRepository postImageJpaRepository;

    @InjectMocks
    private PostImageReadService postImageReadService;

    @Test
    @DisplayName("게시글 이미지 개수 검증 - 정상")
    void validatePostImage_Success() {
        // given
        Long postId = 1L;
        int newImageCount = 2;
        long currentCount = 3;

        when(postImageJpaRepository.countByPostIdAndStatus(postId, Status.ACTIVE))
                .thenReturn(currentCount);

        // when & then (예외 발생하지 않음)
        assertThatCode(() -> postImageReadService.validatePostImage(postId, newImageCount))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("게시글 이미지 개수 검증 - 초과")
    void validatePostImage_Exceeded() {
        // given
        Long postId = 1L;
        int newImageCount = 3;
        long currentCount = 3; // 현재 3개 + 추가 3개 = 6개 (5개 초과)

        when(postImageJpaRepository.countByPostIdAndStatus(postId, Status.ACTIVE))
                .thenReturn(currentCount);

        // when & then
        assertThatThrownBy(() -> postImageReadService.validatePostImage(postId, newImageCount))
                .isInstanceOf(ImageCountExceededException.class);
    }

    @Test
    @DisplayName("게시글 이미지 개수 검증 - 정확히 5개")
    void validatePostImage_Exactly5() {
        // given
        Long postId = 1L;
        int newImageCount = 2;
        long currentCount = 3; // 현재 3개 + 추가 2개 = 5개 (정확히 5개)

        when(postImageJpaRepository.countByPostIdAndStatus(postId, Status.ACTIVE))
                .thenReturn(currentCount);

        // when & then
        assertThatCode(() -> postImageReadService.validatePostImage(postId, newImageCount))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("시작 순서 조회 - 기존 이미지 있음")
    void getStartOrder_WithExistingImages() {
        // given
        Long postId = 1L;
        PostImageEntity lastImage = PostImageEntity.builder()
                .postId(postId)
                .imgUrl("http://example.com/image.jpg")
                .originName("image.jpg")
                .newName("unique-image.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .sort(3)
                .build();

        when(postImageJpaRepository.findTopByPostIdAndStatusOrderBySortDesc(postId, Status.ACTIVE))
                .thenReturn(Optional.of(lastImage));

        // when
        int startOrder = postImageReadService.getStartOrder(postId);

        // then
        assertThat(startOrder).isEqualTo(4);
    }

    @Test
    @DisplayName("시작 순서 조회 - 기존 이미지 없음")
    void getStartOrder_NoExistingImages() {
        // given
        Long postId = 1L;

        when(postImageJpaRepository.findTopByPostIdAndStatusOrderBySortDesc(postId, Status.ACTIVE))
                .thenReturn(Optional.empty());

        // when
        int startOrder = postImageReadService.getStartOrder(postId);

        // then
        assertThat(startOrder).isEqualTo(1);
    }
}
