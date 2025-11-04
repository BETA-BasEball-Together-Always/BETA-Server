package com.beta.presentation.community.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmotionRequest {

    @NotBlank(message = "반응 타입은 필수입니다")
    private String emotionType;
}
