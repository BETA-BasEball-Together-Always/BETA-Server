package com.beta.common.exception.image;

public class InvalidImageTypeException extends RuntimeException {
    public InvalidImageTypeException() {
        super("지원하지 않는 이미지 형식입니다");
    }

    public InvalidImageTypeException(String message) {
        super(message);
    }
}
