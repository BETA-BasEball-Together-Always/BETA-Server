package com.beta.unit.auth;

import com.beta.application.auth.dto.UserDto;
import com.beta.application.auth.service.UserWriteService;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserWriteService 단위 테스트")
class UserWriteServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private UserConsentJpaRepository userConsentJpaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserWriteService userWriteService;

    @Test
    @DisplayName("사용자 저장 시 UserDto를 반환한다")
    void should_returnUserDto_when_saveUser() {
        // given
        BaseballTeamEntity team = TeamFixture.createDoosan();
        UserEntity userEntity = UserFixture.createActiveUser("social_123", "신규유저", team);

        UserDto userDto = UserDto.builder()
                .email("test@example.com")
                .password("rawPassword123")
                .socialId("social_123")
                .nickName("신규유저")
                .socialProvider(SocialProvider.KAKAO)
                .role(UserEntity.UserRole.USER.name())
                .gender(String.valueOf(UserEntity.GenderType.M))
                .age(25)
                .build();

        when(passwordEncoder.encode("rawPassword123")).thenReturn("encryptedPassword");
        when(userJpaRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // when
        UserDto result = userWriteService.saveUser(userDto, team);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getNickName()).isEqualTo("신규유저");
        assertThat(result.getSocialId()).isEqualTo("social_123");
        verify(passwordEncoder).encode("rawPassword123");
        verify(userJpaRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("사용자 저장 시 비밀번호를 암호화한다")
    void should_encryptPassword_when_saveUser() {
        // given
        BaseballTeamEntity team = TeamFixture.createDoosan();
        String rawPassword = "myPassword123!";
        String encryptedPassword = "$2a$10$encryptedPasswordHash";

        UserDto userDto = UserDto.builder()
                .email("user@example.com")
                .password(rawPassword)
                .socialId("social_456")
                .nickName("암호화테스트유저")
                .socialProvider(SocialProvider.NAVER)
                .role(UserEntity.UserRole.USER.name())
                .gender(String.valueOf(UserEntity.GenderType.F))
                .age(35)
                .build();

        UserEntity savedEntity = UserFixture.createActiveUser("social_456", "암호화테스트유저", team);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encryptedPassword);
        when(userJpaRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);

        // when
        userWriteService.saveUser(userDto, team);

        // then
        verify(passwordEncoder).encode(rawPassword);
        verify(userJpaRepository).save(captor.capture());
    }

    @Test
    @DisplayName("동의 정보 저장 시 UserConsentEntity를 저장한다")
    void should_saveUserConsent_when_saveAgreements() {
        // given
        Long userId = 1L;
        Boolean agreeMarketing = true;
        Boolean personalInfoRequired = true;
        ArgumentCaptor<UserConsentEntity> captor = ArgumentCaptor.forClass(UserConsentEntity.class);

        // when
        userWriteService.saveAgreements(agreeMarketing, personalInfoRequired, userId);

        // then
        verify(userConsentJpaRepository).save(captor.capture());
        UserConsentEntity captured = captor.getValue();

        assertThat(captured.getUserId()).isEqualTo(userId);
        assertThat(captured.getAgreeMarketing()).isEqualTo(agreeMarketing);
        assertThat(captured.getPersonalInfoRequired()).isEqualTo(personalInfoRequired);
    }

    @Test
    @DisplayName("마케팅 동의 거부 시에도 동의 정보를 저장한다")
    void should_saveUserConsent_when_saveAgreementsWithMarketingDisagree() {
        // given
        Long userId = 1L;
        Boolean agreeMarketing = false;
        Boolean personalInfoRequired = true;
        ArgumentCaptor<UserConsentEntity> captor = ArgumentCaptor.forClass(UserConsentEntity.class);

        // when
        userWriteService.saveAgreements(agreeMarketing, personalInfoRequired, userId);

        // then
        verify(userConsentJpaRepository).save(captor.capture());
        UserConsentEntity captured = captor.getValue();

        assertThat(captured.getAgreeMarketing()).isFalse();
        assertThat(captured.getPersonalInfoRequired()).isTrue();
    }
}
