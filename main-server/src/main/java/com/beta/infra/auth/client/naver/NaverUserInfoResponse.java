package com.beta.infra.auth.client.naver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
class NaverUserInfoResponse {

    @JsonProperty("response")
    private ResponseData response;

    @Getter
    @NoArgsConstructor
    static class ResponseData {
        private String id;
        private String gender;
        private String age;
    }

    public String getSocialId() {
        return response.getId();
    }

    public String getGender() {
        return response != null ? response.getGender() : null;
    }

    public String getAgeRange() {
        return response != null ? response.getAge() : null;
    }
}
