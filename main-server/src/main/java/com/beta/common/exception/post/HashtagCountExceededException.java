package com.beta.common.exception.post;

public class HashtagCountExceededException extends RuntimeException {
    public HashtagCountExceededException() {
        super("해시태그는 최대 10개까지 가능합니다");
    }
}
