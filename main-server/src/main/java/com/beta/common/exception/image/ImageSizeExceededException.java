package com.beta.common.exception.image;

import com.beta.common.exception.ErrorCode;

public class ImageSizeExceededException extends RuntimeException {
    public ImageSizeExceededException() {
        super(ErrorCode.IMAGE_SIZE_EXCEEDED.getMessage());
    }
}
