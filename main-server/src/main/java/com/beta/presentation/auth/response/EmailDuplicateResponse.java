package com.beta.presentation.auth.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailDuplicateResponse {
    private boolean duplicate;
}
