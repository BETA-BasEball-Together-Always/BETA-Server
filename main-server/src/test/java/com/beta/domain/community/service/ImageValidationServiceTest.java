package com.beta.domain.community.service;

import com.beta.common.exception.image.ImageCountExceededException;
import com.beta.common.exception.image.ImageRequiredException;
import com.beta.common.exception.image.ImageSizeExceededException;
import com.beta.common.exception.image.InvalidImageTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageValidationService 테스트")
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
        void throwExceptionWhenImagesIsNull() {
            // when & then
            assertThatThrownBy(() -> imageValidationService.validateImages(null))
                    .isInstanceOf(ImageRequiredException.class);
        }

        @Test
        @DisplayName("이미지가 빈 리스트이면 ImageRequiredException이 발생한다")
        void throwExceptionWhenImagesIsEmpty() {
            // given
            List<MultipartFile> emptyImages = Collections.emptyList();

            // when & then
            assertThatThrownBy(() -> imageValidationService.validateImages(emptyImages))
                    .isInstanceOf(ImageRequiredException.class);
        }

        @Test
        @DisplayName("이미지 개수가 5개를 초과하면 ImageCountExceededException이 발생한다")
        void throwExceptionWhenImageCountExceedsMax() {
            // given
            List<MultipartFile> images = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                images.add(createValidJpegImage());
            }

            // when & then
            assertThatThrownBy(() -> imageValidationService.validateImages(images))
                    .isInstanceOf(ImageCountExceededException.class);
        }

        @Test
        @DisplayName("이미지 개수가 1개일 때 정상 처리된다")
        void successWhenImageCountIsOne() {
            // given
            List<MultipartFile> images = List.of(createValidJpegImage());

            // when & then
            assertThatCode(() -> imageValidationService.validateImages(images))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("이미지 개수가 5개일 때 정상 처리된다")
        void successWhenImageCountIsFive() {
            // given
            List<MultipartFile> images = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                images.add(createValidJpegImage());
            }

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
        void throwExceptionWhenFileSizeExceedsMax() {
            // given
            long exceedingSize = 11 * 1024 * 1024; // 11MB
            byte[] largeContent = new byte[(int) exceedingSize];
            MockMultipartFile largeImage = new MockMultipartFile(
                    "image",
                    "large.jpg",
                    "image/jpeg",
                    largeContent
            );
            List<MultipartFile> images = List.of(largeImage);

            // when & then
            assertThatThrownBy(() -> imageValidationService.validateImages(images))
                    .isInstanceOf(ImageSizeExceededException.class);
        }

        @Test
        @DisplayName("파일 크기가 10MB 이하일 때 정상 처리된다")
        void successWhenFileSizeIsWithinLimit() {
            // given
            long validSize = 5 * 1024 * 1024; // 5MB
            byte[] jpegHeader = new byte[(int) validSize];
            jpegHeader[0] = (byte) 0xFF;
            jpegHeader[1] = (byte) 0xD8;
            jpegHeader[2] = (byte) 0xFF;
            
            MockMultipartFile validImage = new MockMultipartFile(
                    "image",
                    "valid.jpg",
                    "image/jpeg",
                    jpegHeader
            );
            List<MultipartFile> images = List.of(validImage);

            // when & then
            assertThatCode(() -> imageValidationService.validateImages(images))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("validateMimeType 테스트")
    class ValidateMimeTypeTest {

        @Test
        @DisplayName("JPEG 이미지 (magic number: 0xFF 0xD8 0xFF)는 정상 처리된다")
        void successWithValidJpegImage() {
            // given
            byte[] jpegBytes = new byte[]{
                    (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0
            };
            MockMultipartFile jpegImage = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    "image/jpeg",
                    jpegBytes
            );
            List<MultipartFile> images = List.of(jpegImage);

            // when & then
            assertThatCode(() -> imageValidationService.validateImages(images))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PNG 이미지 (magic number: 0x89 0x50 0x4E 0x47)는 정상 처리된다")
        void successWithValidPngImage() {
            // given
            byte[] pngBytes = new byte[]{
                    (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47
            };
            MockMultipartFile pngImage = new MockMultipartFile(
                    "image",
                    "test.png",
                    "image/png",
                    pngBytes
            );
            List<MultipartFile> images = List.of(pngImage);

            // when & then
            assertThatCode(() -> imageValidationService.validateImages(images))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("잘못된 MIME type의 이미지는 InvalidImageTypeException이 발생한다")
        void throwExceptionWithInvalidMimeType() {
            // given
            byte[] invalidBytes = new byte[]{
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
            };
            MockMultipartFile invalidImage = new MockMultipartFile(
                    "image",
                    "test.txt",
                    "text/plain",
                    invalidBytes
            );
            List<MultipartFile> images = List.of(invalidImage);

            // when & then
            assertThatThrownBy(() -> imageValidationService.validateImages(images))
                    .isInstanceOf(InvalidImageTypeException.class);
        }

        @Test
        @DisplayName("4바이트 미만의 데이터는 InvalidImageTypeException이 발생한다")
        void throwExceptionWhenBytesAreLessThanFour() {
            // given
            byte[] shortBytes = new byte[]{(byte) 0xFF, (byte) 0xD8};
            MockMultipartFile shortImage = new MockMultipartFile(
                    "image",
                    "short.jpg",
                    "image/jpeg",
                    shortBytes
            );
            List<MultipartFile> images = List.of(shortImage);

            // when & then
            assertThatThrownBy(() -> imageValidationService.validateImages(images))
                    .isInstanceOf(InvalidImageTypeException.class);
        }

        @Test
        @DisplayName("GIF 같은 허용되지 않은 이미지 타입은 InvalidImageTypeException이 발생한다")
        void throwExceptionWithNotAllowedImageType() {
            // given - GIF magic number: 0x47 0x49 0x46 0x38
            byte[] gifBytes = new byte[]{
                    (byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38
            };
            MockMultipartFile gifImage = new MockMultipartFile(
                    "image",
                    "test.gif",
                    "image/gif",
                    gifBytes
            );
            List<MultipartFile> images = List.of(gifImage);

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
        void successWithMultipleValidImages() {
            // given
            List<MultipartFile> images = List.of(
                    createValidJpegImage(),
                    createValidPngImage(),
                    createValidJpegImage()
            );

            // when & then
            assertThatCode(() -> imageValidationService.validateImages(images))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("여러 이미지 중 하나라도 유효하지 않으면 InvalidImageTypeException이 발생한다")
        void throwExceptionWhenAnyImageIsInvalid() {
            // given
            List<MultipartFile> images = List.of(
                    createValidJpegImage(),
                    createInvalidImage(),
                    createValidPngImage()
            );

            // when & then
            assertThatThrownBy(() -> imageValidationService.validateImages(images))
                    .isInstanceOf(InvalidImageTypeException.class);
        }
    }

    // Helper methods
    private MockMultipartFile createValidJpegImage() {
        byte[] jpegBytes = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0
        };
        return new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                jpegBytes
        );
    }

    private MockMultipartFile createValidPngImage() {
        byte[] pngBytes = new byte[]{
                (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47
        };
        return new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                pngBytes
        );
    }

    private MockMultipartFile createInvalidImage() {
        byte[] invalidBytes = new byte[]{
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        };
        return new MockMultipartFile(
                "image",
                "invalid.txt",
                "text/plain",
                invalidBytes
        );
    }
}
