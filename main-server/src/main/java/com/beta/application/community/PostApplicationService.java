package com.beta.application.community;

import com.beta.application.auth.service.FindUserService;
import com.beta.application.community.dto.HashtagDto;
import com.beta.application.community.service.HashtagReadService;
import com.beta.application.community.service.PostImageWriteService;
import com.beta.application.community.service.PostReadService;
import com.beta.application.community.service.PostWriteService;
import com.beta.common.exception.idempotency.IdempotencyKeyException;
import com.beta.infra.community.redis.CommunityRedisRepository;
import com.beta.presentation.community.request.PostCreateRequest;
import com.beta.presentation.community.response.PostUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PostApplicationService {

    private final CommunityRedisRepository communityRedisRepository;
    private final PostWriteService postWriteService;
    private final PostImageWriteService postImageWriteService;
    private final FindUserService findUserService;
    private final HashtagReadService hashtagReadService;

    @Transactional
    public PostUploadResponse uploadPost(String idempotencyKey, PostCreateRequest request, Long userId, String teamCode) {
        validateIdempotencyKeyOrThrow(CommunityRedisRepository.ApiPrefix.POST, idempotencyKey);
        findUserService.findUserById(userId); // 사용자 존재 여부 확인
        Long postId = postWriteService.savePost(userId, request.getAllChannel(), request.getContent(), teamCode);
        if(request.getHashtags() != null && !request.getHashtags().isEmpty()) {
            postWriteService.saveHashtags(postId, request.getHashtags());
        }
        if(request.getImages() != null && !request.getImages().isEmpty()) {
            postImageWriteService.publishPostImages(postId, request.getImages());
        }
        return PostUploadResponse.success();
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
