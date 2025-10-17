package com.beta.presentation.auth.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NameDuplicateResponse {

    private boolean duplicate;

}
