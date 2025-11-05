package com.beta.common.exception.image;

public class ImageOrderMismatchException extends RuntimeException {
    public ImageOrderMismatchException() {
        super("정렬할 이미지 개수와 DB 이미지 개수가 일치하지 않습니다");
    }
}
