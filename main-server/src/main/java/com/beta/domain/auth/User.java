package com.beta.domain.auth;

import com.beta.common.provider.SocialProvider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
    private Long id;
    private String socialId;
    private String name;
    private SocialProvider socialProvider;
    private String favoriteTeamCode;
    private String gender;
    private String ageRange;
    private String status;
    private String role;

    public boolean isNewUser() {
        return id == null;
    }
}
