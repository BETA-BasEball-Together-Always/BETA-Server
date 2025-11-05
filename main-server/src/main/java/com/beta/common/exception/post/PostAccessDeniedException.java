package com.beta.common.exception.post;

public class PostAccessDeniedException extends RuntimeException {
    public PostAccessDeniedException() {
        super("게시글에 대한 권한이 없습니다.");
    }
}
