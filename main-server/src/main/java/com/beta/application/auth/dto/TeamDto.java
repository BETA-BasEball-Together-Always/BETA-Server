package com.beta.application.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamDto {
    private String teamCode;
    private String teamNameKr;
    private String teamNameEn;
}
