package com.beta.application.common.service;

import com.beta.common.exception.team.TeamNotFoundException;
import com.beta.infra.common.entity.BaseballTeamEntity;
import com.beta.infra.common.repository.BaseballTeamRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindTeamService 단위 테스트")
class FindTeamServiceTest {

    @Mock
    private BaseballTeamRepository baseballTeamRepository;

    @InjectMocks
    private FindTeamService findTeamService;

    @Test
    @DisplayName("구단 코드로 구단을 조회한다")
    void getBaseballTeamById_success() {
        // given
        String teamCode = "KIA";
        BaseballTeamEntity teamEntity = BaseballTeamEntity.builder()
                .code("KIA")
                .teamNameKr("KIA 타이거즈")
                .teamNameEn("KIA Tigers")
                .homeStadium("광주-기아 챔피언스 필드")
                .stadiumAddress("광주광역시")
                .build();

        when(baseballTeamRepository.findById(teamCode)).thenReturn(Optional.of(teamEntity));

        // when
        BaseballTeamEntity result = findTeamService.getBaseballTeamById(teamCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("KIA");
        assertThat(result.getTeamNameKr()).isEqualTo("KIA 타이거즈");
        verify(baseballTeamRepository, times(1)).findById(teamCode);
    }

    @Test
    @DisplayName("존재하지 않는 구단 코드로 조회 시 TeamNotFoundException이 발생한다")
    void getBaseballTeamById_notFound_throwsException() {
        // given
        String invalidCode = "INVALID";
        when(baseballTeamRepository.findById(invalidCode)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findTeamService.getBaseballTeamById(invalidCode))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessageContaining("해당 구단은 존재하지 않습니다")
                .hasMessageContaining(invalidCode);

        verify(baseballTeamRepository, times(1)).findById(invalidCode);
    }

    @Test
    @DisplayName("모든 야구 구단 목록을 조회한다")
    void getAllBaseballTeams_success() {
        // given
        List<BaseballTeamEntity> teamList = Arrays.asList(
                BaseballTeamEntity.builder()
                        .code("KIA")
                        .teamNameKr("KIA 타이거즈")
                        .teamNameEn("KIA Tigers")
                        .homeStadium("광주-기아 챔피언스 필드")
                        .stadiumAddress("광주광역시")
                        .build(),
                BaseballTeamEntity.builder()
                        .code("LG")
                        .teamNameKr("LG 트윈스")
                        .teamNameEn("LG Twins")
                        .homeStadium("잠실야구장")
                        .stadiumAddress("서울시")
                        .build()
        );

        when(baseballTeamRepository.findAll()).thenReturn(teamList);

        // when
        List<BaseballTeamEntity> result = findTeamService.getAllBaseballTeams();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCode()).isEqualTo("KIA");
        assertThat(result.get(1).getCode()).isEqualTo("LG");
        verify(baseballTeamRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("구단이 없으면 빈 리스트를 반환한다")
    void getAllBaseballTeams_emptyList() {
        // given
        when(baseballTeamRepository.findAll()).thenReturn(Arrays.asList());

        // when
        List<BaseballTeamEntity> result = findTeamService.getAllBaseballTeams();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(baseballTeamRepository, times(1)).findAll();
    }
}
