package com.beta.presentation.community;

import com.beta.application.community.PostApplicationService;
import com.beta.application.community.PostImageApplicationService;
import com.beta.application.community.dto.PostWithImagesDto;
import com.beta.common.security.CustomUserDetails;
import com.beta.presentation.community.request.*;
import com.beta.presentation.community.response.HashtagListResponse;
import com.beta.presentation.community.response.ImageDeleteResponse;
import com.beta.presentation.community.response.PostDeleteResponse;
import com.beta.presentation.community.response.PostImagesResponse;
import com.beta.presentation.community.response.PostUploadResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostImageApplicationService postImageApplicationService;
    private final PostApplicationService postApplicationService;

    @PostMapping(value = "/images", consumes = {"multipart/form-data"})
    public ResponseEntity<List<PostImagesResponse>> uploadImages(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam("images") List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postImageApplicationService.uploadImages(idempotencyKey, images, userDetails.userId()));
    }

    @PostMapping(value = "/{postId}/images", consumes = {"multipart/form-data"})
    public ResponseEntity<List<PostImagesResponse>> addImages(
            @PathVariable Long postId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam("images") List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postImageApplicationService.insertImagesToPost(idempotencyKey, postId, images, userDetails.userId()));
    }

    @DeleteMapping("/{postId}/images")
    public ResponseEntity<ImageDeleteResponse> deleteImages(
            @PathVariable Long postId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ImageDeleteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postImageApplicationService.softDeleteImages(postId, idempotencyKey, request, userDetails.userId()));
    }

    @PostMapping
    public ResponseEntity<PostUploadResponse> uploadPost(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postApplicationService.uploadPost(idempotencyKey, request, userDetails.userId(), userDetails.teamCode()));
    }

    @PatchMapping("/{postId}/content")
    public ResponseEntity<PostUploadResponse> updateContent(
            @PathVariable Long postId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PostContentUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postApplicationService.updatePostContent(postId, idempotencyKey, request, userDetails.userId()));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<PostDeleteResponse> deletePost(
            @PathVariable Long postId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postApplicationService.deletePost(postId, userDetails.userId(), idempotencyKey));
    }

    @PostMapping("/{postId}/emotions")
    public ResponseEntity<?> addOrUpdateEmotion(
            @PathVariable Long postId,
            @Valid @RequestBody EmotionRequest request
    ) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hashtags")
    public ResponseEntity<HashtagListResponse> getHashtags() {
        return ResponseEntity.ok(HashtagListResponse.from(postApplicationService.getHashtags()));
    }

    @GetMapping
    public ResponseEntity<?> getPosts(
            @RequestParam(required = false) String channel,
            @RequestParam(defaultValue = "LATEST") String sortType,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Integer cursorEmotionCount,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostDetail(@PathVariable Long postId) {
        return ResponseEntity.ok().build();
    }
}
