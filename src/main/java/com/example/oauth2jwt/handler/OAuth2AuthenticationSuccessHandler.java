package com.example.oauth2jwt.handler;

import com.example.oauth2jwt.entity.Role;
import com.example.oauth2jwt.entity.User;
import com.example.oauth2jwt.repository.UserRepository;
import com.example.oauth2jwt.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = extractEmail(oAuth2User);
        String name = extractName(oAuth2User);
        String picture = extractPicture(oAuth2User);
        String provider = "google"; // Google OAuth2만 우선 지원
        String providerId = extractProviderId(oAuth2User);
        
        User user = saveOrUpdateUser(email, name, picture, provider, providerId);
        
        String token = jwtUtil.generateToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);
        
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
                .queryParam("token", token)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();
        
        log.info("OAuth2 login success for user: {}", email);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private User saveOrUpdateUser(String email, String name, String picture, String provider, String providerId) {
        User user = userRepository.findByEmail(email)
                .map(existingUser -> existingUser.update(name, picture))
                .orElse(User.builder()
                        .email(email)
                        .name(name)
                        .picture(picture)
                        .role(Role.USER)
                        .provider(provider)
                        .providerId(providerId)
                        .build());
        
        return userRepository.save(user);
    }

    private String extractEmail(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("email");
    }

    private String extractName(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("name");
    }

    private String extractPicture(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("picture");
    }

    private String extractProviderId(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("sub");
    }
}