package com.beta.infra.auth.repository;

import com.beta.infra.auth.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {

    void deleteByUserId(Long userId);
}
