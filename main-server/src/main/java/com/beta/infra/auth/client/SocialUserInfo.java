package com.beta.infra.auth.client;

import lombok.*;

@Getter
@Builder
public class SocialUserInfo {
    private String socialId;
    private String ageRange;
    private String gender;
}
