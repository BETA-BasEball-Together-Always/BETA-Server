package com.beta.auth.presentation.controller;

import com.beta.auth.presentation.dto.request.SocialLoginRequest;
import com.beta.common.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @PostMapping("/login/kakao")
    public ResponseEntity<?> kakaoLogin(@Valid @RequestBody SocialLoginRequest request) {
        // TODO: 카카오 로그인 로직 구현

        return ResponseEntity.ok("");
    }

    @PostMapping("/login/naver")
    public ResponseEntity<?> naverLogin(@Valid @RequestBody SocialLoginRequest request) {
        // TODO: 네이버 로그인 로직 구현

        return ResponseEntity.ok("");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // TODO: 로그아웃 로직 구현

        return ResponseEntity.ok("");
    }
}
