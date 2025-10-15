package com.beta.application.auth.service;

import com.beta.common.provider.SocialProvider;
import com.beta.infra.auth.client.SocialLoginClient;
import com.beta.infra.auth.client.SocialLoginClientFactory;
import com.beta.infra.auth.client.SocialUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindSocialService {

    private final SocialLoginClientFactory clientFactory;

    @Transactional(readOnly = true)
    public SocialUserInfo getSocialUserInfo(String token, SocialProvider socialProvider) {
        SocialLoginClient client = clientFactory.getClient(socialProvider);
        return client.getUserInfo(token);
    }
}
