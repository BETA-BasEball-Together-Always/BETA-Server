package com.beta.common.fixture;

import com.beta.infra.community.entity.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 테스트용 Post 관련 엔티티 생성 유틸리티
 */
public class PostFixture {

    public static PostEntity createPost(Long userId, String content, PostEntity.Channel channel) {
        return PostEntity.builder()
                .userId(userId)
                .content(content)
                .channel(channel.name())
                .build();
    }

    public static PostEntity createPostWithChannel(Long userId, PostEntity.Channel channel) {
        return PostEntity.builder()
                .userId(userId)
                .content("Test content for " + channel)
                .channel(channel.name())
                .build();
    }

    public static PostEntity createSimplePost(Long userId) {
        return PostEntity.builder()
                .userId(userId)
                .content("Simple test post")
                .channel(PostEntity.Channel.ALL.name())
                .build();
    }

    public static PostImageEntity createPostImage(Long postId, int sort) {
        return PostImageEntity.builder()
                .postId(postId)
                .userId(1L)
                .imgUrl("https://storage.googleapis.com/test/image-" + sort + ".jpg")
                .originName("test-image-" + sort + ".jpg")
                .newName("unique-image-" + sort + ".jpg")
                .fileSize(1024L * sort)
                .mimeType("image/jpeg")
                .sort(sort)
                .status(Status.ACTIVE)
                .build();
    }

    public static PostImageEntity createPostImageWithStatus(Long postId, int sort, Status status) {
        return PostImageEntity.builder()
                .postId(postId)
                .userId(1L)
                .imgUrl("https://storage.googleapis.com/test/image-" + sort + ".jpg")
                .originName("test-image-" + sort + ".jpg")
                .newName("unique-image-" + sort + ".jpg")
                .fileSize(1024L * sort)
                .mimeType("image/jpeg")
                .sort(sort)
                .status(status)
                .build();
    }

    public static HashtagEntity createHashtag(String name) {
        return HashtagEntity.builder()
                .tagName(name)
                .build();
    }

    public static List<HashtagEntity> createHashtags(String... names) {
        List<HashtagEntity> hashtags = new ArrayList<>();
        for (String name : names) {
            hashtags.add(createHashtag(name));
        }
        return hashtags;
    }

    /**
     * JPEG 매직 넘버를 포함한 유효한 JPEG 이미지 파일 생성
     */
    public static MockMultipartFile createValidJpegImage() {
        byte[] jpegBytes = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46
        };
        return new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                jpegBytes
        );
    }

    /**
     * PNG 매직 넘버를 포함한 유효한 PNG 이미지 파일 생성
     */
    public static MockMultipartFile createValidPngImage() {
        byte[] pngBytes = new byte[]{
                (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47,
                (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A
        };
        return new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                pngBytes
        );
    }

    /**
     * 여러 개의 유효한 이미지 파일 생성
     */
    public static List<MultipartFile> createValidImages(int count) {
        List<MultipartFile> images = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            images.add(i % 2 == 0 ? createValidJpegImage() : createValidPngImage());
        }
        return images;
    }

    /**
     * 잘못된 MIME 타입의 파일 생성
     */
    public static MockMultipartFile createInvalidImage() {
        byte[] invalidBytes = new byte[]{
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        };
        return new MockMultipartFile(
                "file",
                "invalid.txt",
                "text/plain",
                invalidBytes
        );
    }

    /**
     * 큰 용량의 이미지 파일 생성 (10MB 초과)
     */
    public static MockMultipartFile createLargeImage() {
        long exceedingSize = 11 * 1024 * 1024; // 11MB
        byte[] largeContent = new byte[(int) exceedingSize];
        // JPEG 매직 넘버 추가
        largeContent[0] = (byte) 0xFF;
        largeContent[1] = (byte) 0xD8;
        largeContent[2] = (byte) 0xFF;

        return new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );
    }
}
