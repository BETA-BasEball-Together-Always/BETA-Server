package com.beta.domain.community.service;

import com.beta.common.exception.image.ImageCountExceededException;
import com.beta.common.exception.image.ImageRequiredException;
import com.beta.common.exception.image.ImageSizeExceededException;
import com.beta.common.exception.image.InvalidImageTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ImageValidationService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_IMAGE_COUNT = 5;

    public void validateImages(List<MultipartFile> images) {
        validateImageCount(images);
        images.forEach(this::validateImage);
    }

    private void validateImageCount(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new ImageRequiredException();
        }

        if (images.size() > MAX_IMAGE_COUNT) {
            throw new ImageCountExceededException();
        }
    }

    private void validateImage(MultipartFile image) {
        validateFileSize(image);
        validateMimeType(image);
    }

    private void validateFileSize(MultipartFile image) {
        if (image.getSize() > MAX_FILE_SIZE) {
            log.warn("Image size exceeded: {} bytes (max: {} bytes)", image.getSize(), MAX_FILE_SIZE);
            throw new ImageSizeExceededException();
        }
    }

    private void validateMimeType(MultipartFile image) {
        try {
            byte[] bytes = image.getBytes();
            String detectedMimeType = detectMimeTypeFromBytes(bytes);

            if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
                log.warn("Invalid MIME type detected: {}", detectedMimeType);
                throw new InvalidImageTypeException();
            }
        } catch (IOException e) {
            log.error("Failed to read image bytes", e);
            throw new InvalidImageTypeException();
        }
    }

    private String detectMimeTypeFromBytes(byte[] bytes) {
        if (bytes.length < 4) {
            throw new InvalidImageTypeException();
        }

        if (bytes[0] == (byte) 0xFF &&
                bytes[1] == (byte) 0xD8 &&
                bytes[2] == (byte) 0xFF) {
            return "image/jpeg";
        }

        if (bytes[0] == (byte) 0x89 &&
                bytes[1] == (byte) 0x50 &&
                bytes[2] == (byte) 0x4E &&
                bytes[3] == (byte) 0x47) {
            return "image/png";
        }

        throw new InvalidImageTypeException();
    }
}
