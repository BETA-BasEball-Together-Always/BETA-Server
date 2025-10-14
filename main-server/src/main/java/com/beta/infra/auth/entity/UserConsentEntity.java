package com.beta.infra.auth.entity;

import com.beta.infra.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_consents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserConsentEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "agree_personal_info", nullable = false)
    private Boolean agreePersonalInfo;

    @Column(name = "agree_personal_info_at")
    private LocalDateTime agreePersonalInfoAt;

    @Column(name = "agree_marketing", nullable = false)
    private Boolean agreeMarketing;

    @Column(name = "agree_marketing_at")
    private LocalDateTime agreeMarketingAt;

    @Builder
    public UserConsentEntity(Long userId, Boolean agreePersonalInfo, Boolean agreeMarketing) {
        this.userId = userId;
        this.agreePersonalInfo = agreePersonalInfo;
        this.agreePersonalInfoAt = agreePersonalInfo ? LocalDateTime.now() : null;
        this.agreeMarketing = agreeMarketing;
        this.agreeMarketingAt = agreeMarketing ? LocalDateTime.now() : null;
    }
}
