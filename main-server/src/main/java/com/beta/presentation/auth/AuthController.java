package com.beta.presentation.auth;

import com.beta.application.auth.facade.SocialAuthFacade;
import com.beta.application.auth.dto.LoginResult;
import com.beta.common.provider.SocialProvider;
import com.beta.common.security.CustomUserDetails;
import com.beta.presentation.auth.request.RefreshTokenRequest;
import com.beta.presentation.auth.request.SignupCompleteRequest;
import com.beta.presentation.auth.request.SocialLoginRequest;
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
        SocialLoginResponse response = convertToResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/naver")
    public ResponseEntity<SocialLoginResponse> naverLogin(@Valid @RequestBody SocialLoginRequest request) {
        LoginResult result = socialAuthFacade.processSocialLogin(request.getToken(), SocialProvider.NAVER);
        SocialLoginResponse response = convertToResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/complete")
    public ResponseEntity<SocialLoginResponse> completeSignup(@Valid @RequestBody SignupCompleteRequest request) {
        // TODO: 회원가입 완료 로직 구현
        return ResponseEntity.ok(null);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        // TODO: 토큰 재발급 로직 구현
        return ResponseEntity.ok(new TokenResponse("", ""));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // TODO: 로그아웃 로직 구현
        return ResponseEntity.ok("");
    }

    private SocialLoginResponse convertToResponse(LoginResult result) {
        return SocialLoginResponse.ofLoginResult(result);
    }
}
