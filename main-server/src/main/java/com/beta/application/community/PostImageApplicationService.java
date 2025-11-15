package com.beta.application.community;

import com.beta.application.community.dto.ImageDto;
import com.beta.application.community.service.PostImageReadService;
import com.beta.application.community.service.PostImageWriteService;
import com.beta.application.community.service.PostReadService;
import com.beta.common.exception.image.ImageNotFoundException;
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
public class PostImageApplicationService {

    private final PostImageWriteService postImageWriteService;
    private final PostImageReadService postImageReadService;
    private final PostReadService postReadService;

    public List<PostImagesResponse> uploadImages(List<MultipartFile> images, Long userId) {
        return handleImageUpload(userId, null, images);
    }

    @Transactional
    public List<PostImagesResponse> insertImagesToPost(Long postId, List<MultipartFile> images, Long userId) {
        postReadService.validatePostOwnership(postId, userId);
        postImageReadService.validatePostImage(postId, images.size());
        return handleImageUpload(userId, postId, images);
    }

    public ImageDeleteResponse softDeleteImages(Long postId, ImageDeleteRequest request, Long userId) {
        postReadService.validatePostOwnership(postId, userId);
        List<ImageDto> images = postImageWriteService.softDeleteImages(postId, request.getImageIds());
        if (images.isEmpty()) {
            throw new ImageNotFoundException();
        }
        return ImageDeleteResponse.success(images);
    }

    private List<PostImagesResponse> handleImageUpload(Long userId, Long postId, List<MultipartFile> images) {
        List<ImageDto> uploadImages = new ArrayList<>();
        try {
            uploadImages = postImageWriteService.uploadImages(images, userId);
            return postImageWriteService.saveImagesMetadata(uploadImages, postId, userId).stream()
                    .map(PostImagesResponse::from)
                    .toList();
        } catch (Exception e) {
            if (!uploadImages.isEmpty()) {
                postImageWriteService.deleteImages(uploadImages, userId);
            }
            throw e;
        }
    }
}
