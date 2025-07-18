package com.example.oauth2jwt.controller;

import com.example.oauth2jwt.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    // 🔒 토큰 유효성 검증 API
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(HttpServletRequest request, Authentication authentication) {
        try {
            String token = getJwtFromCookie(request);
            
            if (token != null && jwtUtil.validateToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
                log.info("토큰 검증 성공: {}", email);
                
                return ResponseEntity.ok().body(new VerifyResponse(true, email, "토큰이 유효합니다."));
            } else {
                log.warn("토큰 검증 실패");
                return ResponseEntity.status(401).body(new VerifyResponse(false, null, "토큰이 유효하지 않습니다."));
            }
        } catch (Exception e) {
            log.error("토큰 검증 중 오류 발생", e);
            return ResponseEntity.status(500).body(new VerifyResponse(false, null, "토큰 검증 중 오류가 발생했습니다."));
        }
    }

    // 🔒 로그아웃 API (쿠키 삭제)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        try {
            // accessToken 쿠키 삭제
            Cookie accessCookie = new Cookie("accessToken", null);
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(false); // HTTPS 환경에서는 true로 설정
            accessCookie.setPath("/");
            accessCookie.setMaxAge(0); // 즉시 만료
            
            // refreshToken 쿠키 삭제
            Cookie refreshCookie = new Cookie("refreshToken", null);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false); // HTTPS 환경에서는 true로 설정
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(0); // 즉시 만료
            
            response.addCookie(accessCookie);
            response.addCookie(refreshCookie);
            
            log.info("로그아웃 성공 - 쿠키 삭제 완료");
            return ResponseEntity.ok().body(new LogoutResponse(true, "로그아웃이 완료되었습니다."));
            
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생", e);
            return ResponseEntity.status(500).body(new LogoutResponse(false, "로그아웃 중 오류가 발생했습니다."));
        }
    }

    // 🔧 디버깅용 토큰 정보 조회 API (개발환경에서만 사용)
    @GetMapping("/debug/token")
    @org.springframework.context.annotation.Profile("dev")
    public ResponseEntity<?> debugToken(HttpServletRequest request) {
        try {
            String accessToken = getJwtFromCookie(request, "accessToken");
            String refreshToken = getJwtFromCookie(request, "refreshToken");
            
            if (accessToken != null) {
                String email = jwtUtil.getEmailFromToken(accessToken);
                boolean isValid = jwtUtil.validateToken(accessToken);
                
                return ResponseEntity.ok().body(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken != null ? refreshToken : "없음",
                    "email", email,
                    "valid", isValid,
                    "message", "토큰 정보 조회 성공"
                ));
            } else {
                return ResponseEntity.ok().body(Map.of("message", "토큰이 없습니다."));
            }
        } catch (Exception e) {
            log.error("토큰 디버깅 중 오류 발생", e);
            return ResponseEntity.status(500).body(Map.of("error", "토큰 디버깅 중 오류가 발생했습니다."));
        }
    }

    // 🔒 쿠키에서 JWT 토큰 추출
    private String getJwtFromCookie(HttpServletRequest request) {
        return getJwtFromCookie(request, "accessToken");
    }
    
    // 🔒 쿠키에서 특정 이름의 JWT 토큰 추출
    private String getJwtFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // 응답 DTO 클래스들
    public static class VerifyResponse {
        public boolean valid;
        public String email;
        public String message;

        public VerifyResponse(boolean valid, String email, String message) {
            this.valid = valid;
            this.email = email;
            this.message = message;
        }
    }

    public static class LogoutResponse {
        public boolean success;
        public String message;

        public LogoutResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}