package com.beta.application.auth.service;

import com.beta.application.auth.dto.UserDto;
import com.beta.common.provider.SocialProvider;
import com.beta.infra.auth.entity.UserConsentEntity;
import com.beta.infra.auth.entity.UserEntity;
import com.beta.infra.auth.repository.UserConsentJpaRepository;
import com.beta.infra.auth.repository.UserJpaRepository;
import com.beta.infra.common.entity.BaseballTeamEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaveUserService 단위 테스트")
class SaveUserServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private UserConsentJpaRepository userConsentJpaRepository;

    @InjectMocks
    private SaveUserService saveUserService;

    @Test
    @DisplayName("사용자를 저장하고 UserDto를 반환한다")
    void saveUser_success() {
        // given
        UserDto userDto = UserDto.builder()
                .socialId("kakao_12345")
                .name("홍길동")
                .socialProvider(SocialProvider.KAKAO)
                .favoriteTeamCode("KIA")
                .gender("M")
                .ageRange("20-29")
                .build();

        BaseballTeamEntity teamEntity = BaseballTeamEntity.builder()
                .code("KIA")
                .teamNameKr("KIA 타이거즈")
                .teamNameEn("KIA Tigers")
                .homeStadium("광주-기아 챔피언스 필드")
                .stadiumAddress("광주광역시")
                .build();

        UserEntity savedEntity = UserEntity.builder()
                .socialId("kakao_12345")
                .name("홍길동")
                .socialProvider(SocialProvider.KAKAO)
                .baseballTeam(teamEntity)
                .gender(UserEntity.GenderType.M)
                .ageRange(UserEntity.AgeRange.AGE_20_29)
                .build();

        when(userJpaRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        // when
        UserDto result = saveUserService.saveUser(userDto, teamEntity);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSocialId()).isEqualTo("kakao_12345");
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getFavoriteTeamCode()).isEqualTo("KIA");
        verify(userJpaRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("gender와 ageRange가 null인 경우에도 정상 저장된다")
    void saveUser_withNullGenderAndAge_success() {
        // given
        UserDto userDto = UserDto.builder()
                .socialId("naver_67890")
                .name("김철수")
                .socialProvider(SocialProvider.NAVER)
                .favoriteTeamCode("LG")
                .gender(null)
                .ageRange(null)
                .build();

        BaseballTeamEntity teamEntity = BaseballTeamEntity.builder()
                .code("LG")
                .teamNameKr("LG 트윈스")
                .teamNameEn("LG Twins")
                .homeStadium("잠실야구장")
                .stadiumAddress("서울시")
                .build();

        UserEntity savedEntity = UserEntity.builder()
                .socialId("naver_67890")
                .name("김철수")
                .socialProvider(SocialProvider.NAVER)
                .baseballTeam(teamEntity)
                .gender(null)
                .ageRange(null)
                .build();

        when(userJpaRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        // when
        UserDto result = saveUserService.saveUser(userDto, teamEntity);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("김철수");
        verify(userJpaRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("동의 정보를 저장한다")
    void saveAgreements_success() {
        // given
        Long userId = 1L;
        Boolean agreeMarketing = true;
        Boolean agreePersonalInfo = true;

        ArgumentCaptor<UserConsentEntity> captor = ArgumentCaptor.forClass(UserConsentEntity.class);

        // when
        saveUserService.saveAgreements(agreeMarketing, agreePersonalInfo, userId);

        // then
        verify(userConsentJpaRepository, times(1)).save(captor.capture());
        
        UserConsentEntity saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getAgreeMarketing()).isTrue();
        assertThat(saved.getAgreePersonalInfo()).isTrue();
    }

    @Test
    @DisplayName("마케팅 동의 false로 저장 가능하다")
    void saveAgreements_marketingFalse_success() {
        // given
        Long userId = 2L;
        Boolean agreeMarketing = false;
        Boolean agreePersonalInfo = true;

        ArgumentCaptor<UserConsentEntity> captor = ArgumentCaptor.forClass(UserConsentEntity.class);

        // when
        saveUserService.saveAgreements(agreeMarketing, agreePersonalInfo, userId);

        // then
        verify(userConsentJpaRepository, times(1)).save(captor.capture());
        
        UserConsentEntity saved = captor.getValue();
        assertThat(saved.getAgreeMarketing()).isFalse();
        assertThat(saved.getAgreePersonalInfo()).isTrue();
    }
}
