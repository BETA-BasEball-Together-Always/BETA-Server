package com.beta.unit.community;

import com.beta.application.community.dto.HashtagDto;
import com.beta.application.community.service.HashtagReadService;
import com.beta.common.fixture.PostFixture;
import com.beta.infra.community.entity.HashtagEntity;
import com.beta.infra.community.repository.HashtagJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HashtagReadService 단위 테스트")
class HashtagReadServiceTest {

    @Mock
    private HashtagJpaRepository hashtagJpaRepository;

    @InjectMocks
    private HashtagReadService hashtagReadService;

    @Test
    @DisplayName("모든 해시태그 조회 시 HashtagDto 리스트를 반환한다")
    void should_returnHashtagDtoList_when_getAllHashtags() {
        // given
        List<HashtagEntity> hashtagEntities = PostFixture.createHashtags("야구", "응원", "경기");
        when(hashtagJpaRepository.findAll()).thenReturn(hashtagEntities);

        // when
        List<HashtagDto> result = hashtagReadService.getAllHashtags();

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(HashtagDto::getName)
                .containsExactlyInAnyOrder("야구", "응원", "경기");
        verify(hashtagJpaRepository).findAll();
    }

    @Test
    @DisplayName("해시태그가 없을 때 빈 리스트를 반환한다")
    void should_returnEmptyList_when_getAllHashtagsWithNoHashtags() {
        // given
        when(hashtagJpaRepository.findAll()).thenReturn(List.of());

        // when
        List<HashtagDto> result = hashtagReadService.getAllHashtags();

        // then
        assertThat(result).isEmpty();
        verify(hashtagJpaRepository).findAll();
    }
}
