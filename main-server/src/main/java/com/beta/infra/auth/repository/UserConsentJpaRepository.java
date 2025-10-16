package com.beta.infra.auth.repository;

import com.beta.infra.auth.entity.UserConsentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConsentJpaRepository extends JpaRepository<UserConsentEntity, Long> {

}
