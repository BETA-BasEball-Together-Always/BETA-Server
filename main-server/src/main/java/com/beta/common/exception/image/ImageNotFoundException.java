package com.beta.common.exception.image;

public class ImageNotFoundException extends RuntimeException {
    public ImageNotFoundException() {
        super("삭제할 이미지를 찾을 수 없습니다");
    }
}
