package com.beta.infra.community.entity;

public enum Status {
    PENDING,     // 검토 중
    ACTIVE,      // 정상 공개
    MARKED_FOR_DELETION,
    DELETED,     // 삭제됨
    HIDDEN,      // 관리자에 의해 숨김
    REPORTED     // 신고되어 검토 중
}
