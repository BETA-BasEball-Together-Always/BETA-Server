package com.beta.application.community.service;

import com.beta.application.community.dto.ImageDto;
import com.beta.common.exception.image.ImageUploadFailedException;
import com.beta.domain.community.service.ImageValidationService;
import com.beta.infra.community.entity.PostImageEntity;
import com.beta.infra.community.entity.PostImageErrorEntity;
import com.beta.infra.community.gcs.GcsStorageClient;
import com.beta.infra.community.repository.ImageErrorJpaRepository;
import com.beta.infra.community.repository.PostImageJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostImageService {

    private final ImageValidationService imageValidationService;
    private final GcsStorageClient gcsStorageClient;
    private final PostImageJpaRepository postImageJpaRepository;
    private final ImageErrorJpaRepository imageErrorJpaRepository;

    public List<ImageDto> uploadImages(List<MultipartFile> images, Long userId) {
        List<ImageDto> imageDtoList = new ArrayList<>();
        try {
            imageValidationService.validateImages(images);

            for (int i = 0; i < images.size(); i++) {
                ImageDto imageDto = gcsStorageClient.upload(images.get(i), i+1, userId);
                imageDtoList.add(imageDto);
            }

            return imageDtoList;
        } catch (Exception e) {
            for(ImageDto dto : imageDtoList) {
                try {
                    boolean deleted = gcsStorageClient.delete(dto.getNewName());
                    if (!deleted) saveImageError(dto.getImgUrl(), dto.getNewName(),userId);
                } catch (Exception ex) {
                    saveImageError(dto.getImgUrl(), dto.getNewName(), userId);
                    log.error("Failed to delete image during rollback: {}", dto.getImgUrl(), ex);
                }
            }
            throw new ImageUploadFailedException("이미지 업로드 중 오류가 발생했습니다", e);
        }
    }

    public void deleteImages(List<ImageDto> images, Long userId) {
        for (ImageDto image : images) {
            try {
                boolean deleted = gcsStorageClient.delete(image.getNewName());
                if(!deleted) saveImageError(image.getImgUrl(),image.getNewName(), userId);
            } catch (Exception e) {
                saveImageError(image.getImgUrl(), image.getNewName(), userId);
                log.error("Failed to delete image: {}", image.getImgUrl(), e);
            }
        }
    }

    private void saveImageError(String imageUrl, String fileName, Long userId) {
        imageErrorJpaRepository.save(
                PostImageErrorEntity.builder().imageUrl(imageUrl).fileName(fileName).userId(userId).build()
        );
    }

    @Transactional
    public List<ImageDto> saveImagesMetadata(List<ImageDto> uploadImages) {
        List<PostImageEntity> postImageList = uploadImages.stream()
                .map(image -> PostImageEntity.builder()
                    .imgUrl(image.getImgUrl())
                    .originName(image.getOriginName())
                    .newName(image.getNewName())
                    .fileSize(image.getFileSize())
                    .mimeType(image.getMimeType())
                    .order(image.getOrder())
                    .build())
                .toList();

        return postImageJpaRepository.saveAll(postImageList).stream()
                .map(entity -> ImageDto.builder()
                        .postImageId(entity.getId())
                        .imgUrl(entity.getImgUrl())
                        .originName(entity.getOriginName())
                        .newName(entity.getNewName())
                        .fileSize(entity.getFileSize())
                        .mimeType(entity.getMimeType())
                        .order(entity.getOrder())
                        .build())
                .toList();
    }
}
