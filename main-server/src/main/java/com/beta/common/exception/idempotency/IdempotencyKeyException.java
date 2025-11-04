package com.beta.common.exception.idempotency;

import com.beta.common.exception.ErrorCode;

public class IdempotencyKeyException extends RuntimeException {
    public IdempotencyKeyException() {
        super(ErrorCode.IDEMPOTENCY_KEY_DUPLICATE.getMessage());
    }
}
