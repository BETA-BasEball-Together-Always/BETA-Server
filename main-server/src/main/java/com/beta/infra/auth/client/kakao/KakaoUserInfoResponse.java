package com.beta.infra.auth.client.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
class KakaoUserInfoResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @NoArgsConstructor
    static class KakaoAccount {

        @JsonProperty("age_range")
        private String ageRange;

        @JsonProperty("gender")
        private String gender;
    }
}
