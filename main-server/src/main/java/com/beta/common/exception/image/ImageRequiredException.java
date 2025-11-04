package com.beta.common.exception.image;

import com.beta.common.exception.ErrorCode;

public class ImageRequiredException extends RuntimeException {
    public ImageRequiredException() {
        super(ErrorCode.IMAGE_REQUIRED.getMessage());
    }
}
