package com.beta.presentation.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginRequest {

    @NotBlank(message = "토큰은 필수입니다")
    private String token;

    public SocialLoginRequest(String token) {
        this.token = token;
    }
}
