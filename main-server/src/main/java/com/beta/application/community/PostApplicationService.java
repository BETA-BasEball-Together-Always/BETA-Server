package com.beta.application.community;

import com.beta.application.community.dto.ImageDto;
import com.beta.application.community.service.PostImageReadService;
import com.beta.application.community.service.PostImageWriteService;
import com.beta.application.community.service.PostReadService;
import com.beta.common.exception.idempotency.IdempotencyKeyException;
import com.beta.common.exception.image.ImageNotFoundException;
import com.beta.common.exception.image.ImageUploadFailedException;
import com.beta.infra.community.redis.CommunityRedisRepository;
import com.beta.presentation.community.request.ImageDeleteRequest;
import com.beta.presentation.community.request.ImageOrderUpdateRequest;
import com.beta.presentation.community.response.ImageDeleteResponse;
import com.beta.presentation.community.response.PostImagesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostApplicationService {

    private final CommunityRedisRepository communityRedisRepository;
    private final PostImageWriteService postImageWriteService;
    private final PostImageReadService postImageReadService;
    private final PostReadService postReadService;

    public PostImagesResponse uploadImages(String idempotencyKey, List<MultipartFile> images, Long userId) {
        validateIdempotencyKeyOrThrow(CommunityRedisRepository.ApiPrefix.IMAGE, idempotencyKey);
        return handleImageUpload(
                CommunityRedisRepository.ApiPrefix.IMAGE,
                idempotencyKey,
                userId,
                null,
                images,
                1
        );
    }

    @Transactional
    public PostImagesResponse insertImagesToPost(String idempotencyKey, Long postId, List<MultipartFile> images, Long userId) {
        postReadService.validatePostOwnership(postId, userId);
        postImageReadService.validatePostImage(postId, images.size());
        validateIdempotencyKeyOrThrow(CommunityRedisRepository.ApiPrefix.IMAGE_ADD, idempotencyKey);

        return handleImageUpload(
                CommunityRedisRepository.ApiPrefix.IMAGE_ADD,
                idempotencyKey,
                userId,
                postId,
                images,
                postImageReadService.getStartOrder(postId)
        );
    }

    public ImageDeleteResponse softDeleteImages(Long postId, String idempotencyKey, ImageDeleteRequest request, Long userId) {
        postReadService.validatePostOwnership(postId, userId);
        validateIdempotencyKeyOrThrow(CommunityRedisRepository.ApiPrefix.IMAGE_DELETE, idempotencyKey);

        List<ImageDto> images = postImageWriteService.softDeleteImages(postId, request.getImageIds());
        if (images.isEmpty()) {
            throw new ImageNotFoundException();
        }
        return ImageDeleteResponse.success(images);
    }

    public PostImagesResponse updateImageOrder(Long postId, String idempotencyKey, ImageOrderUpdateRequest request, Long userId) {
        validateIdempotencyKeyOrThrow(CommunityRedisRepository.ApiPrefix.IMAGE_ORDER, idempotencyKey);
        postReadService.validatePostOwnership(postId, userId);

        try {
            return PostImagesResponse.from(postImageWriteService.updateImageOrder(postId, request.getImageOrders()));
        } catch (Exception e) {
            communityRedisRepository.delete(CommunityRedisRepository.ApiPrefix.IMAGE_ORDER, idempotencyKey);
            throw e;
        }
    }

    private void validateIdempotencyKeyOrThrow(CommunityRedisRepository.ApiPrefix image, String idempotencyKey) {
        if (!communityRedisRepository.trySetIdempotencyKey(image, idempotencyKey)) {
            throw new IdempotencyKeyException();
        }
    }

    private PostImagesResponse handleImageUpload(CommunityRedisRepository.ApiPrefix prefix, String idempotencyKey,
                                                Long userId, Long postId, List<MultipartFile> images, int startOrder) {
        List<ImageDto> uploadImages = List.of();
        try{
            uploadImages = postImageWriteService.uploadImages(images, userId, startOrder);
            return PostImagesResponse.from(postImageWriteService.saveImagesMetadata(uploadImages, postId));
        } catch(ImageUploadFailedException e){
            communityRedisRepository.delete(prefix, idempotencyKey);
            throw e;
        } catch(Exception e) {
            communityRedisRepository.delete(prefix, idempotencyKey);
            if(!uploadImages.isEmpty()) {
                postImageWriteService.deleteImages(uploadImages, userId);
            }
            throw e;
        }
    }
}
