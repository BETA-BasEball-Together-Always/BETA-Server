package com.beta.application.community;

import com.beta.application.auth.service.FindUserService;
import com.beta.application.community.dto.HashtagDto;
import com.beta.application.community.service.HashtagReadService;
import com.beta.application.community.service.PostWriteService;
import com.beta.presentation.community.request.PostContentUpdateRequest;
import com.beta.presentation.community.request.PostCreateRequest;
import com.beta.presentation.community.response.PostDeleteResponse;
import com.beta.presentation.community.response.PostUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PostApplicationService {

    private final PostWriteService postWriteService;
    private final FindUserService findUserService;
    private final HashtagReadService hashtagReadService;

    public PostUploadResponse uploadPost(PostCreateRequest request, Long userId, String teamCode) {
        findUserService.findUserById(userId); // 사용자 존재 여부 확인
        postWriteService.savePost(userId, request.getAllChannel(), request.getContent(), teamCode, request.getHashtags(), request.getImages());
        return PostUploadResponse.success();
    }

    public PostUploadResponse updatePostContent(Long postId, PostContentUpdateRequest request, Long userId) {
        findUserService.findUserById(userId);
        postWriteService.updatePostContentAndHashtags(userId, postId, request.getContent(), request.getHashtags(), request.getDeleteHashtagIds());
        return PostUploadResponse.success();
    }

    public PostDeleteResponse deletePost(Long postId, Long userId) {
        postWriteService.softDeletePost(postId, userId);
        return PostDeleteResponse.success();
    }

    public List<HashtagDto> getHashtags() {
        return hashtagReadService.getAllHashtags();
    }
}
