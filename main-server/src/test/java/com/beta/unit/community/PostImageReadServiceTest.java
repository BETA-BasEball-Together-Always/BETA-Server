package com.beta.unit.community;

import com.beta.application.community.service.PostImageReadService;
import com.beta.common.exception.image.ImageCountExceededException;
import com.beta.infra.community.entity.Status;
import com.beta.infra.community.repository.PostImageJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostImageReadService 단위 테스트")
class PostImageReadServiceTest {

    @Mock
    private PostImageJpaRepository postImageJpaRepository;

    @InjectMocks
    private PostImageReadService postImageReadService;

    @Test
    @DisplayName("게시글 이미지 개수 검증 시 총 이미지 개수가 5개 이하이면 정상 처리된다")
    void should_pass_when_validatePostImageWithValidCount() {
        // given
        Long postId = 1L;
        int addingSize = 2;
        long existingCount = 3L;

        when(postImageJpaRepository.countByPostIdAndStatus(postId, Status.ACTIVE))
                .thenReturn(existingCount);

        // when & then
        assertThatCode(() -> postImageReadService.validatePostImage(postId, addingSize))
                .doesNotThrowAnyException();

        verify(postImageJpaRepository).countByPostIdAndStatus(postId, Status.ACTIVE);
    }

    @Test
    @DisplayName("게시글 이미지 개수 검증 시 총 이미지 개수가 5개를 초과하면 ImageCountExceededException을 발생시킨다")
    void should_throwImageCountExceededException_when_validatePostImageExceedsMaxCount() {
        // given
        Long postId = 1L;
        int addingSize = 3;
        long existingCount = 3L; // 3 + 3 = 6 > 5

        when(postImageJpaRepository.countByPostIdAndStatus(postId, Status.ACTIVE))
                .thenReturn(existingCount);

        // when & then
        assertThatThrownBy(() -> postImageReadService.validatePostImage(postId, addingSize))
                .isInstanceOf(ImageCountExceededException.class);

        verify(postImageJpaRepository).countByPostIdAndStatus(postId, Status.ACTIVE);
    }

    @Test
    @DisplayName("게시글 이미지 개수 검증 시 총 이미지 개수가 정확히 5개이면 정상 처리된다")
    void should_pass_when_validatePostImageWithExactlyFiveImages() {
        // given
        Long postId = 1L;
        int addingSize = 2;
        long existingCount = 3L; // 3 + 2 = 5

        when(postImageJpaRepository.countByPostIdAndStatus(postId, Status.ACTIVE))
                .thenReturn(existingCount);

        // when & then
        assertThatCode(() -> postImageReadService.validatePostImage(postId, addingSize))
                .doesNotThrowAnyException();

        verify(postImageJpaRepository).countByPostIdAndStatus(postId, Status.ACTIVE);
    }

    @Test
    @DisplayName("기존 이미지가 없는 게시글에 5개 추가 시 정상 처리된다")
    void should_pass_when_validatePostImageWithNoExistingImages() {
        // given
        Long postId = 1L;
        int addingSize = 5;
        long existingCount = 0L;

        when(postImageJpaRepository.countByPostIdAndStatus(postId, Status.ACTIVE))
                .thenReturn(existingCount);

        // when & then
        assertThatCode(() -> postImageReadService.validatePostImage(postId, addingSize))
                .doesNotThrowAnyException();

        verify(postImageJpaRepository).countByPostIdAndStatus(postId, Status.ACTIVE);
    }

    @Test
    @DisplayName("기존 이미지가 5개인 게시글에 1개 추가 시 ImageCountExceededException을 발생시킨다")
    void should_throwException_when_validatePostImageWithMaxExistingImages() {
        // given
        Long postId = 1L;
        int addingSize = 1;
        long existingCount = 5L; // 5 + 1 = 6 > 5

        when(postImageJpaRepository.countByPostIdAndStatus(postId, Status.ACTIVE))
                .thenReturn(existingCount);

        // when & then
        assertThatThrownBy(() -> postImageReadService.validatePostImage(postId, addingSize))
                .isInstanceOf(ImageCountExceededException.class);

        verify(postImageJpaRepository).countByPostIdAndStatus(postId, Status.ACTIVE);
    }
}
