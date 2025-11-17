package com.beta.common.exception.comment;

public class CommentDepthExceededException extends RuntimeException {
    public CommentDepthExceededException() {
        super("대댓글은 한 단계까지만 가능합니다.");
    }
}
