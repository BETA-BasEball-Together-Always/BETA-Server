package com.beta.application.community.service;

import com.beta.application.community.dto.HashtagDto;
import com.beta.infra.community.repository.HashtagJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashtagReadService {

    private final HashtagJpaRepository hashtagJpaRepository;

    @Cacheable(value = "hashtags", key = "'all'")
    @Transactional(readOnly = true)
    public List<HashtagDto> getAllHashtags() {
        return hashtagJpaRepository.findAll().stream()
                .map(HashtagDto::from)
                .toList();
    }
}
