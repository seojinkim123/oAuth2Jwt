package com.example.oauth2jwt.service;

import com.example.oauth2jwt.dto.UserDto;
import com.example.oauth2jwt.entity.User;
import com.example.oauth2jwt.repository.UserRepository;
import com.example.oauth2jwt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public Map<String, String> refreshToken(String refreshToken) {
        Map<String, String> result = new HashMap<>();
        
        try {
            if (jwtUtil.validateToken(refreshToken)) {
                String email = jwtUtil.getEmailFromToken(refreshToken);
                
                Optional<User> userOptional = userRepository.findByEmail(email);
                if (userOptional.isPresent()) {
                    String newAccessToken = jwtUtil.generateToken(email);
                    String newRefreshToken = jwtUtil.generateRefreshToken(email);
                    
                    result.put("accessToken", newAccessToken);
                    result.put("refreshToken", newRefreshToken);
                    result.put("message", "토큰이 성공적으로 갱신되었습니다.");
                } else {
                    result.put("error", "사용자를 찾을 수 없습니다.");
                }
            } else {
                result.put("error", "유효하지 않은 리프레시 토큰입니다.");
            }
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생", e);
            result.put("error", "토큰 갱신 중 오류가 발생했습니다.");
        }
        
        return result;
    }

    public Optional<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            
            String email = authentication.getName();
            return userRepository.findByEmail(email).map(UserDto::from);
        }
        
        return Optional.empty();
    }

    public boolean isTokenValid(String token) {
        return jwtUtil.validateToken(token);
    }

    public String getEmailFromToken(String token) {
        return jwtUtil.getEmailFromToken(token);
    }
}