package com.beta.application.auth.service;

import com.beta.common.provider.SocialProvider;
import com.beta.infra.auth.client.SocialLoginClientFactory;
import com.beta.infra.auth.client.SocialUserInfo;
import com.beta.infra.auth.client.kakao.KakaoLoginClient;
import com.beta.infra.auth.client.naver.NaverLoginClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindSocialService 테스트")
class FindSocialServiceTest {

    @Mock
    private NaverLoginClient naverLoginClient;

    @Mock
    private KakaoLoginClient kakaoLoginClient;

    private SocialLoginClientFactory clientFactory;
    private FindSocialService findSocialService;

    @BeforeEach
    void setUp() {
        clientFactory = new SocialLoginClientFactory(
                List.of(naverLoginClient, kakaoLoginClient)
        );

        findSocialService = new FindSocialService(clientFactory);
    }

    @Test
    @DisplayName("네이버 소셜 로그인으로 사용자 정보를 조회한다")
    void getSocialUserInfoWithNaver() {
        // given
        String token = "sample_token";
        SocialUserInfo expectedUserInfo = SocialUserInfo.builder()
                .socialId("naver123")
                .gender("M")
                .ageRange("20-29")
                .build();

        when(naverLoginClient.supportedProvider()).thenReturn(SocialProvider.NAVER);
        when(naverLoginClient.getUserInfo(token)).thenReturn(expectedUserInfo);

        // when
        SocialUserInfo result = findSocialService.getSocialUserInfo(token, SocialProvider.NAVER);

        // then
        assertThat(result.getSocialId()).isEqualTo("naver123");
        assertThat(result.getGender()).isEqualTo("M");
        verify(naverLoginClient).getUserInfo(token);
    }

    @Test
    @DisplayName("카카오 소셜 로그인으로 사용자 정보를 조회한다")
    void getSocialUserInfoWithKakao() {
        // given
        String token = "kakao_token";
        SocialUserInfo expectedUserInfo = SocialUserInfo.builder()
                .socialId("kakao456")
                .gender("F")
                .ageRange("30-39")
                .build();

        when(kakaoLoginClient.supportedProvider()).thenReturn(SocialProvider.KAKAO);
        when(kakaoLoginClient.getUserInfo(token)).thenReturn(expectedUserInfo);

        // when
        SocialUserInfo result = findSocialService.getSocialUserInfo(token, SocialProvider.KAKAO);

        // then
        assertThat(result.getSocialId()).isEqualTo("kakao456");
        verify(kakaoLoginClient).getUserInfo(token);
    }
}
