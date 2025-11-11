package com.beta.unit.community;

import com.beta.common.exception.image.ImageCountExceededException;
import com.beta.common.exception.image.ImageRequiredException;
import com.beta.common.exception.image.ImageSizeExceededException;
import com.beta.common.exception.image.InvalidImageTypeException;
import com.beta.common.fixture.PostFixture;
import com.beta.domain.community.service.ImageValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageValidationService 단위 테스트")
class ImageValidationServiceTest {

    private ImageValidationService imageValidationService;

    @BeforeEach
    void setUp() {
        imageValidationService = new ImageValidationService();
    }

    @Nested
    @DisplayName("validateImageCount 테스트")
    class ValidateImageCountTest {

        @Test
        @DisplayName("이미지가 null이면 ImageRequiredException이 발생한다")
        void should_throwImageRequiredException_when_imagesIsNull() {
            // when & then
            assertThatThrownBy(() -> imageValidationService.validateImages(null))
                    .isInstanceOf(ImageRequiredException.class);
        }

        @Test
        @DisplayName("이미지가 빈 리스트이면 ImageRequiredException이 발생한다")
        void should_throwImageRequiredException_when_imagesIsEmpty() {
            // given
            List<MultipartFile> emptyImages = Collections.emptyList();

            // when & then
            assertThatThrownBy(() -> imageValidationService.validateImages(emptyImages))
                    .isInstanceOf(ImageRequiredException.class);
        }

        @Test
        @DisplayName("이미지 개수가 5개를 초과하면 ImageCountExceededException이 발생한다")
        void should_throwImageCountExceededException_when_imageCountExceedsMax() {
            // given
            List<MultipartFile> images = PostFixture.createValidImages(6);

            // when & then
            assertThatThrownBy(() -> imageValidationService.validateImages(images))
                    .isInstanceOf(ImageCountExceededException.class);
        }

        @Test
        @DisplayName("이미지 개수가 1개일 때 정상 처리된다")
        void should_pass_when_imageCountIsOne() {
            // given
            List<MultipartFile> images = List.of(PostFixture.createValidJpegImage());

            // when & then
            assertThatCode(() -> imageValidationService.validateImages(images))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("이미지 개수가 5개일 때 정상 처리된다")
        void should_pass_when_imageCountIsFive() {
            // given
            List<MultipartFile> images = PostFixture.createValidImages(5);

            // when & then
            assertThatCode(() -> imageValidationService.validateImages(images))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("validateFileSize 테스트")
    class ValidateFileSizeTest {

        @Test
        @DisplayName("파일 크기가 10MB를 초과하면 ImageSizeExceededException이 발생한다")
        void should_throwImageSizeExceededException_when_fileSizeExceedsMax() {
            // given
            List<MultipartFile> images = List.of(PostFixture.createLargeImage());

            // when & then
            assertThatThrownBy(() -> imageValidationService.validateImages(images))
                    .isInstanceOf(ImageSizeExceededException.class);
        }

        @Test
        @DisplayName("파일 크기가 10MB 이하일 때 정상 처리된다")
        void should_pass_when_fileSizeIsWithinLimit() {
            // given
            List<MultipartFile> images = List.of(PostFixture.createValidJpegImage());

            // when & then
            assertThatCode(() -> imageValidationService.validateImages(images))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("validateMimeType 테스트")
    class ValidateMimeTypeTest {

        @Test
        @DisplayName("JPEG 이미지는 정상 처리된다")
        void should_pass_when_validJpegImage() {
            // given
            List<MultipartFile> images = List.of(PostFixture.createValidJpegImage());

            // when & then
            assertThatCode(() -> imageValidationService.validateImages(images))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PNG 이미지는 정상 처리된다")
        void should_pass_when_validPngImage() {
            // given
            List<MultipartFile> images = List.of(PostFixture.createValidPngImage());

            // when & then
            assertThatCode(() -> imageValidationService.validateImages(images))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("잘못된 MIME type의 이미지는 InvalidImageTypeException이 발생한다")
        void should_throwInvalidImageTypeException_when_invalidMimeType() {
            // given
            List<MultipartFile> images = List.of(PostFixture.createInvalidImage());

            // when & then
            assertThatThrownBy(() -> imageValidationService.validateImages(images))
                    .isInstanceOf(InvalidImageTypeException.class);
        }
    }

    @Nested
    @DisplayName("통합 테스트")
    class IntegrationTest {

        @Test
        @DisplayName("여러 개의 유효한 이미지를 검증할 때 정상 처리된다")
        void should_pass_when_multipleValidImages() {
            // given
            List<MultipartFile> images = List.of(
                    PostFixture.createValidJpegImage(),
                    PostFixture.createValidPngImage(),
                    PostFixture.createValidJpegImage()
            );

            // when & then
            assertThatCode(() -> imageValidationService.validateImages(images))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("여러 이미지 중 하나라도 유효하지 않으면 InvalidImageTypeException이 발생한다")
        void should_throwInvalidImageTypeException_when_anyImageIsInvalid() {
            // given
            List<MultipartFile> images = List.of(
                    PostFixture.createValidJpegImage(),
                    PostFixture.createInvalidImage(),
                    PostFixture.createValidPngImage()
            );

            // when & then
            assertThatThrownBy(() -> imageValidationService.validateImages(images))
                    .isInstanceOf(InvalidImageTypeException.class);
        }
    }
}
