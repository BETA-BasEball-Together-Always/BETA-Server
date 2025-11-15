package com.beta.presentation.community;

import com.beta.application.community.PostApplicationService;
import com.beta.application.community.PostImageApplicationService;
import com.beta.common.idempotency.Idempotent;
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

    @Idempotent(ttlSeconds = 3)
    @PostMapping(value = "/images", consumes = {"multipart/form-data"})
    public ResponseEntity<List<PostImagesResponse>> uploadImages(
            @RequestParam("images") List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postImageApplicationService.uploadImages(images, userDetails.userId()));
    }

    @Idempotent(ttlSeconds = 3)
    @PostMapping(value = "/{postId}/images", consumes = {"multipart/form-data"})
    public ResponseEntity<List<PostImagesResponse>> addImages(
            @PathVariable Long postId,
            @RequestParam("images") List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postImageApplicationService.insertImagesToPost(postId, images, userDetails.userId()));
    }

    @Idempotent(ttlSeconds = 3)
    @DeleteMapping("/{postId}/images")
    public ResponseEntity<ImageDeleteResponse> deleteImages(
            @PathVariable Long postId,
            @Valid @RequestBody ImageDeleteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postImageApplicationService.softDeleteImages(postId, request, userDetails.userId()));
    }

    @Idempotent(ttlSeconds = 5)
    @PostMapping
    public ResponseEntity<PostUploadResponse> uploadPost(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postApplicationService.uploadPost(request, userDetails.userId(), userDetails.teamCode()));
    }

    @Idempotent(ttlSeconds = 5)
    @PatchMapping("/{postId}/content")
    public ResponseEntity<PostUploadResponse> updateContent(
            @PathVariable Long postId,
            @Valid @RequestBody PostContentUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postApplicationService.updatePostContent(postId, request, userDetails.userId()));
    }

    @Idempotent(ttlSeconds = 5)
    @DeleteMapping("/{postId}")
    public ResponseEntity<PostDeleteResponse> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postApplicationService.deletePost(postId, userDetails.userId()));
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
