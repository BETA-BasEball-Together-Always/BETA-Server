package com.beta.unit.auth;

import com.beta.application.auth.service.SocialUserInfoService;
import com.beta.common.provider.SocialProvider;
import com.beta.infra.auth.client.SocialLoginClient;
import com.beta.infra.auth.client.SocialLoginClientFactory;
import com.beta.infra.auth.client.SocialUserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SocialUserInfoService 단위 테스트")
class SocialUserInfoServiceTest {

    @Mock
    private SocialLoginClientFactory clientFactory;

    @Mock
    private SocialLoginClient socialLoginClient;

    @InjectMocks
    private SocialUserInfoService socialUserInfoService;

    @Test
    @DisplayName("카카오 소셜 로그인 시 사용자 정보를 반환한다")
    void should_returnSocialUserInfo_when_fetchSocialUserInfoWithKakao() {
        // given
        String token = "kakao_access_token";
        SocialProvider provider = SocialProvider.KAKAO;

        SocialUserInfo expectedUserInfo = SocialUserInfo.builder()
                .socialId("kakao_12345")
                .build();

        when(clientFactory.getClient(provider)).thenReturn(socialLoginClient);
        when(socialLoginClient.getUserInfo(token)).thenReturn(expectedUserInfo);

        // when
        SocialUserInfo result = socialUserInfoService.fetchSocialUserInfo(token, provider);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSocialId()).isEqualTo("kakao_12345");
        verify(clientFactory).getClient(provider);
        verify(socialLoginClient).getUserInfo(token);
    }

    @Test
    @DisplayName("네이버 소셜 로그인 시 사용자 정보를 반환한다")
    void should_returnSocialUserInfo_when_fetchSocialUserInfoWithNaver() {
        // given
        String token = "naver_access_token";
        SocialProvider provider = SocialProvider.NAVER;

        SocialUserInfo expectedUserInfo = SocialUserInfo.builder()
                .socialId("naver_67890")
                .build();

        when(clientFactory.getClient(provider)).thenReturn(socialLoginClient);
        when(socialLoginClient.getUserInfo(token)).thenReturn(expectedUserInfo);

        // when
        SocialUserInfo result = socialUserInfoService.fetchSocialUserInfo(token, provider);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSocialId()).isEqualTo("naver_67890");
        verify(clientFactory).getClient(provider);
        verify(socialLoginClient).getUserInfo(token);
    }
}
