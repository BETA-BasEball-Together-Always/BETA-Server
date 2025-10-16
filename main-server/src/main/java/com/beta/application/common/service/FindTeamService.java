package com.beta.application.common.service;

import com.beta.common.exception.team.TeamNotFoundException;
import com.beta.infra.common.entity.BaseballTeamEntity;
import com.beta.infra.common.repository.BaseballTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindTeamService {

    private final BaseballTeamRepository baseballTeamRepository;

    public BaseballTeamEntity getBaseballTeamById(String code) {
        return baseballTeamRepository.findById(code).orElseThrow(() -> new TeamNotFoundException("해당 구단은 존재하지 않습니다 : " + code));
    }

    @Transactional(readOnly = true)
    public List<BaseballTeamEntity> getAllBaseballTeams() {
        return baseballTeamRepository.findAll();
    }
}
