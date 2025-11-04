package com.beta.infra.community.gcs;

import com.beta.application.community.dto.ImageDto;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class GcsStorageClient {

    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    public ImageDto upload(MultipartFile file, int order, Long userId) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename(), userId);

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        return ImageDto.builder()
                .imgUrl(String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName))
                .order(order)
                .newName(fileName)
                .originName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .build();
    }

    public boolean delete(String fileName) throws IOException {
        BlobId blobId = BlobId.of(bucketName, fileName);
        return storage.delete(blobId);
    }

    private String generateFileName(String originalFileName, Long userId) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String customFileName = "BETA-image-" + System.currentTimeMillis() + "-" + userId;

        return today + "/" + customFileName + extension;
    }
}
