package com.beta.common.security;

import com.beta.common.exception.ErrorCode;
import com.beta.presentation.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            String token = extractTokenFromRequest(request);
            
            if (token != null) {
                // 2. 토큰 만료 확인
                if (jwtTokenProvider.isTokenExpired(token)) {
                    sendErrorResponse(response, ErrorResponse.of(ErrorCode.EXPIRED_TOKEN));
                    return;
                }
                
                // 3. 토큰 유효성 확인
                if (!jwtTokenProvider.isTokenValid(token)) {
                    sendErrorResponse(response, ErrorResponse.of(ErrorCode.INVALID_TOKEN));
                    return;
                }
                
                // 4. 토큰에서 사용자 ID와 role 추출
                String userId = jwtTokenProvider.getSubject(token);
                String role = jwtTokenProvider.getClaim(token, JwtTokenProvider.ClaimEnum.ROLE.name(), String.class);

                if (userId != null && role != null) {
                    // 5. Spring Security 인증 객체 생성 (CustomUserDetails 사용)
                    CustomUserDetails userDetails = new CustomUserDetails(Long.valueOf(userId), role);
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. SecurityContext에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("Successfully authenticated user: {}", userId);
                } else {
                    sendErrorResponse(response, ErrorResponse.of(ErrorCode.INVALID_TOKEN_USER_INFO));
                    return;
                }
            }
            
        } catch (Exception e) {
            log.debug("JWT authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            sendErrorResponse(response, ErrorResponse.of(ErrorCode.TOKEN_ERROR));
            return;
        }
        
        // 7. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 JWT 토큰 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * JWT 에러 응답 전송 (ErrorResponse 객체 사용)
     */
    private void sendErrorResponse(HttpServletResponse response, ErrorResponse errorResponse) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // 인증이 필요하지 않은 경로들
        return path.startsWith("/api/auth/login/kakao") ||
               path.startsWith("/api/auth/login/naver") ||
                path.startsWith("/api/auth/name/check") ||
               path.startsWith("/api/auth/signup/complete") ||  // 회원가입 완료도 제외
               path.startsWith("/api/auth/refresh");              // 리프레시도 제외
    }
}
