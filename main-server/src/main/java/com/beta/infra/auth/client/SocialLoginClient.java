package com.beta.infra.auth.client;

import com.beta.common.provider.SocialProvider;

public interface SocialLoginClient {

    SocialProvider supportedProvider();

    SocialUserInfo getUserInfo(String accessToken);
}
