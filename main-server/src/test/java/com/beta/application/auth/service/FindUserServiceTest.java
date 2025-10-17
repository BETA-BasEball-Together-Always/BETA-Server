package com.beta.application.auth.service;

import com.beta.common.provider.SocialProvider;
import com.beta.infra.auth.repository.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindUserService 테스트")
class FindUserServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private FindUserService findUserService;

    @Test
    @DisplayName("findBySocialIdAndSocialProvider 메서드가 호출된다.")
    void findUserBySocialId() {
        // given
        String socialId = "sample_social_id";

        // when
        findUserService.findUserBySocialId(socialId, SocialProvider.KAKAO);

        // then
        verify(userJpaRepository).findBySocialIdAndSocialProvider(socialId, SocialProvider.KAKAO);
    }

    @Test
    @DisplayName("existsByName 메서드가 호출된다.")
    void isNameDuplicate() {
        // given
        String name = "sample_name";

        // when
        findUserService.isNameDuplicate(name);

        // then
        verify(userJpaRepository).existsByName(name);
    }
}
