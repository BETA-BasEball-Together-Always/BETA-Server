package com.beta.presentation.community;

import com.beta.application.community.PostApplicationService;
import com.beta.common.security.CustomUserDetails;
import com.beta.presentation.community.request.*;
import com.beta.presentation.community.response.ImageDeleteResponse;
import com.beta.presentation.community.response.PostImagesResponse;
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

    private final PostApplicationService postApplicationService;

    @PostMapping(value = "/images", consumes = {"multipart/form-data"})
    public ResponseEntity<List<PostImagesResponse>> uploadImages(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam("images") List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postApplicationService.uploadImages(idempotencyKey, images, userDetails.userId()));
    }

    @PostMapping(value = "/{postId}/images", consumes = {"multipart/form-data"})
    public ResponseEntity<List<PostImagesResponse>> addImages(
            @PathVariable Long postId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam("images") List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postApplicationService.insertImagesToPost(idempotencyKey, postId, images, userDetails.userId()));
    }

    @DeleteMapping("/{postId}/images")
    public ResponseEntity<ImageDeleteResponse> deleteImages(
            @PathVariable Long postId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ImageDeleteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postApplicationService.softDeleteImages(postId, idempotencyKey, request, userDetails.userId()));
    }

    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PostCreateRequest request
    ) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{postId}/content")
    public ResponseEntity<?> updateContent(
            @PathVariable Long postId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PostContentUpdateRequest request
    ) {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/emotions")
    public ResponseEntity<?> addOrUpdateEmotion(
            @PathVariable Long postId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody EmotionRequest request
    ) {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}/emotions")
    public ResponseEntity<Void> removeEmotion(
            @PathVariable Long postId,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ResponseEntity.noContent().build();
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
