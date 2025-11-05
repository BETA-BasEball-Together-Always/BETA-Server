package com.beta.application.community.service;

import com.beta.application.community.dto.ImageDto;
import com.beta.common.exception.image.ImageOrderMismatchException;
import com.beta.common.exception.image.ImageUploadFailedException;
import com.beta.domain.community.service.ImageValidationService;
import com.beta.infra.community.entity.PostImageEntity;
import com.beta.infra.community.entity.PostImageErrorEntity;
import com.beta.infra.community.entity.Status;
import com.beta.infra.community.gcs.GcsStorageClient;
import com.beta.infra.community.repository.ImageErrorJpaRepository;
import com.beta.infra.community.repository.PostImageJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostImageWriteService {

    private final ImageValidationService imageValidationService;
    private final GcsStorageClient gcsStorageClient;
    private final PostImageJpaRepository postImageJpaRepository;
    private final ImageErrorJpaRepository imageErrorJpaRepository;

    @Transactional
    public List<ImageDto> uploadImages(List<MultipartFile> images, Long userId, int sortStart) {
        List<ImageDto> imageDtoList = new ArrayList<>();
        try {
            imageValidationService.validateImages(images);

            for (MultipartFile image : images) {
                ImageDto imageDto = gcsStorageClient.upload(image, sortStart++, userId);
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

    @Transactional
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

    @Transactional
    public List<ImageDto> saveImagesMetadata(List<ImageDto> uploadImages, Long postId) {
        List<PostImageEntity> postImageList = uploadImages.stream()
                .map(image -> PostImageEntity.builder()
                    .postId(postId)
                    .imgUrl(image.getImgUrl())
                    .originName(image.getOriginName())
                    .newName(image.getNewName())
                    .fileSize(image.getFileSize())
                    .mimeType(image.getMimeType())
                    .sort(image.getSort())
                    .build())
                .toList();

        return postImageJpaRepository.saveAll(postImageList).stream()
                .map(ImageDto::toDto)
                .toList();
    }

    @Transactional
    public List<ImageDto> softDeleteImages(Long postId, List<Long> imageIds) {
        List<PostImageEntity> images = postImageJpaRepository.findAllByIdInAndPostIdAndStatus(imageIds, postId, Status.ACTIVE);
        images.forEach(PostImageEntity::softDelete);
        return postImageJpaRepository.saveAll(images).stream()
                .map(ImageDto::toDto)
                .toList();
    }

    @Transactional
    public List<ImageDto> updateImageOrder(Long postId, List<Long> imageOrders) {
        List<PostImageEntity> images = postImageJpaRepository.findAllByIdInAndPostIdAndStatusIn(
                imageOrders, postId, List.of(Status.PENDING, Status.ACTIVE)
        );

        if (images.size() != imageOrders.size()) {
            throw new ImageOrderMismatchException();
        }

        Map<Long, Integer> orderMap = IntStream.range(0, imageOrders.size())
                .boxed()
                .collect(Collectors.toMap(imageOrders::get, i -> i + 1));

        images.forEach(image -> image.sortUpdate(orderMap.get(image.getId())));

        return postImageJpaRepository.saveAll(images).stream()
                .map(ImageDto::toDto)
                .toList();
    }

    private void saveImageError(String imageUrl, String fileName, Long userId) {
        imageErrorJpaRepository.save(
                PostImageErrorEntity.builder().imageUrl(imageUrl).fileName(fileName).userId(userId).build()
        );
    }
}
