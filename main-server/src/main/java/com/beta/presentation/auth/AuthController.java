package com.beta.presentation.auth;

import com.beta.application.auth.facade.SocialAuthFacade;
import com.beta.application.auth.dto.LoginResult;
import com.beta.common.provider.SocialProvider;
import com.beta.common.security.CustomUserDetails;
import com.beta.presentation.auth.request.EmailLoginRequest;
import com.beta.presentation.auth.request.RefreshTokenRequest;
import com.beta.presentation.auth.request.SignupCompleteRequest;
import com.beta.presentation.auth.request.SocialLoginRequest;
import com.beta.presentation.auth.response.EmailDuplicateResponse;
import com.beta.presentation.auth.response.LogoutResponse;
import com.beta.presentation.auth.response.NameDuplicateResponse;
import com.beta.presentation.auth.response.SocialLoginResponse;
import com.beta.presentation.auth.response.TokenResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SocialAuthFacade socialAuthFacade;

    @PostMapping("/login/{provider}")
    public ResponseEntity<SocialLoginResponse> socialLogin(
            @PathVariable("provider") SocialProvider provider,
            @Valid @RequestBody SocialLoginRequest request) {
        LoginResult result = socialAuthFacade.processSocialLogin(request.getToken(), provider);
        return ResponseEntity.ok(SocialLoginResponse.ofLoginResult(result));
    }

    @PostMapping("/login/email")
    public ResponseEntity<SocialLoginResponse> emailLogin(@Valid @RequestBody EmailLoginRequest request) {
        LoginResult result = socialAuthFacade.processEmailLogin(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(SocialLoginResponse.ofLoginResult(result));
    }

    @PostMapping("/signup/complete")
    public ResponseEntity<SocialLoginResponse> completeSignup(@Valid @RequestBody SignupCompleteRequest request) {
        LoginResult result = socialAuthFacade.completeSignup(request);
        return ResponseEntity.ok(SocialLoginResponse.ofLoginResult(result));
    }

    @GetMapping("/nickname/duplicate-check")
    public ResponseEntity<NameDuplicateResponse> checkNicknameDuplicate(
            @RequestParam("nickname") @NotBlank String nickname) {
        boolean isDuplicate = socialAuthFacade.isNameDuplicate(nickname);
        return ResponseEntity.ok(NameDuplicateResponse.builder().duplicate(isDuplicate).build());
    }

    @GetMapping("/email/duplicate-check")
    public ResponseEntity<EmailDuplicateResponse> checkEmailDuplicate(
            @RequestParam("email") @NotBlank String email) {
        boolean isDuplicate = socialAuthFacade.isEmailDuplicate(email);
        return ResponseEntity.ok(EmailDuplicateResponse.builder().duplicate(isDuplicate).build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshAccessToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = socialAuthFacade.refreshTokens(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        socialAuthFacade.logout(userDetails.userId());
        return ResponseEntity.ok(LogoutResponse.success());
    }
}
