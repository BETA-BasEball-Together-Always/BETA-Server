package com.beta.application.community;

import com.beta.application.auth.service.FindUserService;
import com.beta.application.community.dto.HashtagDto;
import com.beta.application.community.service.HashtagReadService;
import com.beta.application.community.service.PostImageWriteService;
import com.beta.application.community.service.PostReadService;
import com.beta.application.community.service.PostWriteService;
import com.beta.common.exception.idempotency.IdempotencyKeyException;
import com.beta.infra.community.redis.CommunityRedisRepository;
import com.beta.presentation.community.request.PostContentUpdateRequest;
import com.beta.presentation.community.request.PostCreateRequest;
import com.beta.presentation.community.response.PostUploadResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PostApplicationService {

    private final CommunityRedisRepository communityRedisRepository;
    private final PostWriteService postWriteService;
    private final FindUserService findUserService;
    private final HashtagReadService hashtagReadService;

    public PostUploadResponse uploadPost(String idempotencyKey, PostCreateRequest request, Long userId, String teamCode) {
        validateIdempotencyKeyOrThrow(CommunityRedisRepository.ApiPrefix.POST, idempotencyKey);
        findUserService.findUserById(userId); // 사용자 존재 여부 확인
        postWriteService.savePost(userId, request.getAllChannel(), request.getContent(), teamCode, request.getHashtags(), request.getImages());
        return PostUploadResponse.success();
    }

    public PostUploadResponse updatePostContent(Long postId, String idempotencyKey, PostContentUpdateRequest request, Long userId) {
        validateIdempotencyKeyOrThrow(CommunityRedisRepository.ApiPrefix.POST_UPDATE, idempotencyKey);
        findUserService.findUserById(userId);
        postWriteService.updatePostContentAndHashtags(userId, postId, request.getContent(), request.getHashtags(), request.getDeleteHashtags());
        return PostUploadResponse.success();
    }

    public void deletePost(Long postId, Long userId, String idempotencyKey) {
        validateIdempotencyKeyOrThrow(CommunityRedisRepository.ApiPrefix.POST_DELETE, idempotencyKey);
        postWriteService.softDeletePost(postId, userId);
    }

    public List<HashtagDto> getHashtags() {
        return hashtagReadService.getAllHashtags();
    }

    private void validateIdempotencyKeyOrThrow(CommunityRedisRepository.ApiPrefix image, String idempotencyKey) {
        if (!communityRedisRepository.trySetIdempotencyKey(image, idempotencyKey)) {
            throw new IdempotencyKeyException();
        }
    }
}
