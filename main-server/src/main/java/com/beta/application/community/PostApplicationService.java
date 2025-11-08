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
import com.beta.presentation.community.response.ImageDeleteResponse;
import com.beta.presentation.community.response.PostImagesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostApplicationService {

    private final CommunityRedisRepository communityRedisRepository;
    private final PostImageWriteService postImageWriteService;
    private final PostImageReadService postImageReadService;
    private final PostReadService postReadService;

    public List<PostImagesResponse> uploadImages(String idempotencyKey, List<MultipartFile> images, Long userId) {
        validateIdempotencyKeyOrThrow(CommunityRedisRepository.ApiPrefix.IMAGE, idempotencyKey);
        return handleImageUpload(
                CommunityRedisRepository.ApiPrefix.IMAGE,
                idempotencyKey,
                userId,
                null,
                images
        );
    }

    @Transactional
    public List<PostImagesResponse> insertImagesToPost(String idempotencyKey, Long postId, List<MultipartFile> images, Long userId) {
        postReadService.validatePostOwnership(postId, userId);
        postImageReadService.validatePostImage(postId, images.size());
        validateIdempotencyKeyOrThrow(CommunityRedisRepository.ApiPrefix.IMAGE_ADD, idempotencyKey);

        return handleImageUpload(
                CommunityRedisRepository.ApiPrefix.IMAGE_ADD,
                idempotencyKey,
                userId,
                postId,
                images
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

    private void validateIdempotencyKeyOrThrow(CommunityRedisRepository.ApiPrefix image, String idempotencyKey) {
        if (!communityRedisRepository.trySetIdempotencyKey(image, idempotencyKey)) {
            throw new IdempotencyKeyException();
        }
    }

    private List<PostImagesResponse> handleImageUpload(CommunityRedisRepository.ApiPrefix prefix, String idempotencyKey,
                                                Long userId, Long postId, List<MultipartFile> images) {
        List<ImageDto> uploadImages = new ArrayList<>();
        try{
            uploadImages = postImageWriteService.uploadImages(images, userId);
            return postImageWriteService.saveImagesMetadata(uploadImages, postId).stream()
                    .map(PostImagesResponse::from)
                    .toList();
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
