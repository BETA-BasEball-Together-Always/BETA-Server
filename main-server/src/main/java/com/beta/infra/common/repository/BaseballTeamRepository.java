package com.beta.infra.common.repository;

import com.beta.infra.common.entity.BaseballTeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseballTeamRepository extends JpaRepository<BaseballTeamEntity, String> {

}
