package com.beta.application.auth.dto;

import com.beta.common.provider.SocialProvider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String password;
    private String socialId;
    private String nickName;
    private SocialProvider socialProvider;
    private String favoriteTeamCode;
    private String favoriteTeamName;
    private String role;
    private String gender;
    private Integer age;
}
