package com.beta.presentation.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupCompleteRequest {
    @NotBlank(message = "가입 토큰은 필수입니다")
    private String signupToken;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "응원팀 코드는 필수입니다")
    private String favoriteTeamCode;

    private Boolean agreePersonalInfo;

    private Boolean agreeMarketing;
}
