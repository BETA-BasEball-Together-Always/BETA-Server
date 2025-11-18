package com.beta.presentation.community;

import com.beta.application.community.CommentApplicationService;
import com.beta.common.idempotency.Idempotent;
import com.beta.common.security.CustomUserDetails;
import com.beta.presentation.community.request.CommentCreateRequest;
import com.beta.presentation.community.request.CommentUpdateRequest;
import com.beta.presentation.community.response.CommentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentApplicationService commentApplicationService;

    @Idempotent(ttlSeconds = 2)
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(commentApplicationService.createComment(
                postId,
                request.getContent(),
                request.getParentId(),
                userDetails.userId()
        ));
    }

    @Idempotent(ttlSeconds = 2)
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(commentApplicationService.updateComment(
                commentId,
                request.getContent(),
                userDetails.userId()
        ));
    }

    @Idempotent(ttlSeconds = 2)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommentResponse> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(commentApplicationService.deleteComment(
                commentId,
                userDetails.userId()
        ));
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentResponse> commentLike(
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(commentApplicationService.commentLike(commentId, userDetails.userId()));
    }
}
