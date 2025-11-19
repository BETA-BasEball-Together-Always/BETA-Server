package com.beta.domain.auth;

import com.beta.common.provider.SocialProvider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
    private Long id;
    private String email;
    private String password;
    private String socialId;
    private String nickName;
    private SocialProvider socialProvider;
    private String favoriteTeamCode;
    private String favoriteTeamName;
    private String gender;
    private Integer age;
    private String status;
    private String role;

    public boolean isNewUser() {
        return id == null;
    }
}
