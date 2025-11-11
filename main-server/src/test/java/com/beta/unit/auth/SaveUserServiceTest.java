package com.beta.unit.auth;

import com.beta.application.auth.dto.UserDto;
import com.beta.application.auth.service.SaveUserService;
import com.beta.common.fixture.TeamFixture;
import com.beta.common.fixture.UserFixture;
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
    @DisplayName("사용자 저장 시 UserDto를 반환한다")
    void should_returnUserDto_when_saveUser() {
        // given
        BaseballTeamEntity team = TeamFixture.createDoosan();
        UserEntity userEntity = UserFixture.createActiveUser("social_123", "신규유저", team);

        UserDto userDto = UserDto.builder()
                .socialId("social_123")
                .name("신규유저")
                .socialProvider(SocialProvider.KAKAO)
                .role(UserEntity.UserRole.USER.name())
                .gender(String.valueOf(UserEntity.GenderType.M))
                .ageRange(UserEntity.AgeRange.AGE_20_29.name())
                .build();

        when(userJpaRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // when
        UserDto result = saveUserService.saveUser(userDto, team);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("신규유저");
        assertThat(result.getSocialId()).isEqualTo("social_123");
        verify(userJpaRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("동의 정보 저장 시 UserConsentEntity를 저장한다")
    void should_saveUserConsent_when_saveAgreements() {
        // given
        Long userId = 1L;
        Boolean agreeMarketing = true;
        Boolean agreePersonalInfo = true;
        ArgumentCaptor<UserConsentEntity> captor = ArgumentCaptor.forClass(UserConsentEntity.class);

        // when
        saveUserService.saveAgreements(agreeMarketing, agreePersonalInfo, userId);

        // then
        verify(userConsentJpaRepository).save(captor.capture());
        UserConsentEntity captured = captor.getValue();

        assertThat(captured.getUserId()).isEqualTo(userId);
        assertThat(captured.getAgreeMarketing()).isEqualTo(agreeMarketing);
        assertThat(captured.getAgreePersonalInfo()).isEqualTo(agreePersonalInfo);
    }

    @Test
    @DisplayName("마케팅 동의 거부 시에도 동의 정보를 저장한다")
    void should_saveUserConsent_when_saveAgreementsWithMarketingDisagree() {
        // given
        Long userId = 1L;
        Boolean agreeMarketing = false;
        Boolean agreePersonalInfo = true;
        ArgumentCaptor<UserConsentEntity> captor = ArgumentCaptor.forClass(UserConsentEntity.class);

        // when
        saveUserService.saveAgreements(agreeMarketing, agreePersonalInfo, userId);

        // then
        verify(userConsentJpaRepository).save(captor.capture());
        UserConsentEntity captured = captor.getValue();

        assertThat(captured.getAgreeMarketing()).isFalse();
        assertThat(captured.getAgreePersonalInfo()).isTrue();
    }
}
