package com.beta.common.fixture;

import com.beta.infra.common.entity.BaseballTeamEntity;

import java.math.BigDecimal;

/**
 * 테스트용 BaseballTeam 엔티티 생성 유틸리티
 */
public class TeamFixture {

    public static BaseballTeamEntity createDoosan() {
        return BaseballTeamEntity.builder()
                .code("DOOSAN")
                .teamNameEn("Doosan Bears")
                .teamNameKr("두산 베어스")
                .homeStadium("Jamsil Baseball Stadium")
                .stadiumAddress("서울특별시 송파구 올림픽로 25")
                .latitude(new BigDecimal("37.5121"))
                .longitude(new BigDecimal("127.0718"))
                .foundedYear(1982)
                .build();
    }

    public static BaseballTeamEntity createLG() {
        return BaseballTeamEntity.builder()
                .code("LG")
                .teamNameEn("LG Twins")
                .teamNameKr("LG 트윈스")
                .homeStadium("Jamsil Baseball Stadium")
                .stadiumAddress("서울특별시 송파구 올림픽로 25")
                .latitude(new BigDecimal("37.5121"))
                .longitude(new BigDecimal("127.0718"))
                .foundedYear(1982)
                .build();
    }

    public static BaseballTeamEntity createKIA() {
        return BaseballTeamEntity.builder()
                .code("KIA")
                .teamNameEn("KIA Tigers")
                .teamNameKr("KIA 타이거즈")
                .homeStadium("Gwangju-Kia Champions Field")
                .stadiumAddress("광주광역시 북구 서림로 10")
                .latitude(new BigDecimal("35.1681"))
                .longitude(new BigDecimal("126.8890"))
                .foundedYear(1982)
                .build();
    }

    public static BaseballTeamEntity createSamsung() {
        return BaseballTeamEntity.builder()
                .code("SAMSUNG")
                .teamNameEn("Samsung Lions")
                .teamNameKr("삼성 라이온즈")
                .homeStadium("Daegu Samsung Lions Park")
                .stadiumAddress("대구광역시 수성구 야구전설로 1")
                .latitude(new BigDecimal("35.8410"))
                .longitude(new BigDecimal("128.6817"))
                .foundedYear(1982)
                .build();
    }

    public static BaseballTeamEntity createWithCode(String code) {
        return BaseballTeamEntity.builder()
                .code(code)
                .teamNameEn("Test Team")
                .teamNameKr("테스트 팀")
                .homeStadium("Test Stadium")
                .stadiumAddress("Test Address")
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .foundedYear(2024)
                .build();
    }
}
