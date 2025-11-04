package com.beta.application.community;

import com.beta.application.community.dto.ImageDto;
import com.beta.application.community.service.PostImageService;
import com.beta.common.exception.idempotency.IdempotencyKeyException;
import com.beta.common.exception.image.ImageUploadFailedException;
import com.beta.infra.community.redis.CommunityRedisRepository;
import com.beta.presentation.community.response.PostImagesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostApplicationService {

    private final CommunityRedisRepository communityRedisRepository;
    private final PostImageService postImageService;

    public PostImagesResponse uploadImages(String idempotencyKey, List<MultipartFile> images, Long userId) {
        if (!communityRedisRepository.trySetIdempotencyKey(CommunityRedisRepository.ApiPrefix.IMAGE, idempotencyKey)) {
            throw new IdempotencyKeyException();
        }

        List<ImageDto> uploadImages = List.of();

        try{
            uploadImages = postImageService.uploadImages(images, userId);
            return PostImagesResponse.of(postImageService.saveImagesMetadata(uploadImages));
        } catch(ImageUploadFailedException e){
            communityRedisRepository.delete(CommunityRedisRepository.ApiPrefix.IMAGE, idempotencyKey);
            throw e;
        } catch(Exception e) {
            communityRedisRepository.delete(CommunityRedisRepository.ApiPrefix.IMAGE, idempotencyKey);
            if(!uploadImages.isEmpty()) {
                postImageService.deleteImages(uploadImages, userId);
            }
            throw e;
        }
    }
}
