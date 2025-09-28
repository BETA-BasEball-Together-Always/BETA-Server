package com.beta.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginRequest {

    @NotBlank(message = "액세스 토큰은 필수입니다")
    private String accessToken;

    public SocialLoginRequest(String accessToken) {
        this.accessToken = accessToken;
    }
}
