package com.beta.application.community.service;

import com.beta.infra.community.entity.CommentEntity;
import com.beta.infra.community.repository.CommentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentReadService {

    private final CommentJpaRepository commentJpaRepository;

}
