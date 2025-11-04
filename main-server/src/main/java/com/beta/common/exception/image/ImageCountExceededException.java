package com.beta.common.exception.image;

import com.beta.common.exception.ErrorCode;

public class ImageCountExceededException extends RuntimeException {
    public ImageCountExceededException() {
        super(ErrorCode.IMAGE_COUNT_EXCEEDED.getMessage());
    }
}
