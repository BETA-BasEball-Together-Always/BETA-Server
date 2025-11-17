package com.beta.common.exception.comment;

public class CommentAccessDeniedException extends RuntimeException {
    public CommentAccessDeniedException() {
        super("댓글에 대한 권한이 없습니다.");
    }
}
