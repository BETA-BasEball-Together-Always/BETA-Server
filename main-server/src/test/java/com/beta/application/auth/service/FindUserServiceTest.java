package com.beta.application.auth.service;

import com.beta.common.provider.SocialProvider;
import com.beta.infra.auth.repository.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        String socialId = "sample_social_id";
        findUserService.findUserBySocialId(socialId, SocialProvider.KAKAO);
        userJpaRepository.findBySocialIdAndSocialProvider(socialId, SocialProvider.KAKAO);
    }
}
