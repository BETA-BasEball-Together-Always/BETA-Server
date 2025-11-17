package com.beta.application.community.service;

import com.beta.application.community.dto.CommentDto;
import com.beta.infra.community.entity.Status;
import com.beta.infra.community.repository.CommentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentReadService {

    private final CommentJpaRepository commentJpaRepository;

    public List<CommentDto> getCommentsByPostId(Long postId) {
        return commentJpaRepository.findAllByPostIdAndStatus(postId, Status.ACTIVE)
                .stream()
                .map(CommentDto::from)
                .toList();
    }

    public long getCommentCount(Long postId) {
        return commentJpaRepository.countByPostIdAndStatus(postId, Status.ACTIVE);
    }
}
