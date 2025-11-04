package com.beta.common.exception.image;

public class ImageUploadFailedException extends RuntimeException {
    public ImageUploadFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
