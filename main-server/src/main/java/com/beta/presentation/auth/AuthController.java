package com.beta.presentation.auth;

import com.beta.application.auth.facade.SocialAuthFacade;
import com.beta.application.auth.dto.LoginResult;
import com.beta.common.provider.SocialProvider;
import com.beta.common.security.CustomUserDetails;
import com.beta.presentation.auth.request.RefreshTokenRequest;
import com.beta.presentation.auth.request.SignupCompleteRequest;
import com.beta.presentation.auth.request.SocialLoginRequest;
import com.beta.presentation.auth.response.LogoutResponse;
import com.beta.presentation.auth.response.NameDuplicateResponse;
import com.beta.presentation.auth.response.SocialLoginResponse;
import com.beta.presentation.auth.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SocialAuthFacade socialAuthFacade;

    @PostMapping("/login/kakao")
    public ResponseEntity<SocialLoginResponse> kakaoLogin(@Valid @RequestBody SocialLoginRequest request) {
        LoginResult result = socialAuthFacade.processSocialLogin(request.getToken(), SocialProvider.KAKAO);
        SocialLoginResponse response = toResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/naver")
    public ResponseEntity<SocialLoginResponse> naverLogin(@Valid @RequestBody SocialLoginRequest request) {
        LoginResult result = socialAuthFacade.processSocialLogin(request.getToken(), SocialProvider.NAVER);
        SocialLoginResponse response = toResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/complete")
    public ResponseEntity<SocialLoginResponse> completeSignup(@Valid @RequestBody SignupCompleteRequest request) {
        LoginResult result = socialAuthFacade.completeSignup(request);
        SocialLoginResponse response = toResponse(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/check")
    public ResponseEntity<NameDuplicateResponse> checkNameDuplicate(@RequestParam("name") String name) {
        boolean isDuplicate = socialAuthFacade.isNameDuplicate(name);
        return ResponseEntity.ok(NameDuplicateResponse.builder().duplicate(isDuplicate).build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = socialAuthFacade.refreshTokens(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(LogoutResponse.failed("인증 정보가 없습니다."));
        }
        socialAuthFacade.logout(userDetails.userId());
        return ResponseEntity.ok(LogoutResponse.success());
    }

    private SocialLoginResponse toResponse(LoginResult result) {
        return SocialLoginResponse.ofLoginResult(result);
    }
}
