package com.we.hirehub.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        log.debug("🔍 JWT 필터 실행: {}", path);

        // ✅ 인증이 필요 없는 공개 경로 목록
        if (isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return; // 🚫 JWT 검사 스킵
        }

        try {
            // ✅ Authorization 헤더에서 토큰 추출
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = header.substring(7);
            log.debug("🪙 추출된 토큰: {}", token.substring(0, Math.min(15, token.length())) + "...");

            if (StringUtils.hasText(token) && tokenProvider.validate(token)) {
                Long userId = tokenProvider.getUserId(token);
                String email = tokenProvider.getUsername(token); // subject가 email임

                List<GrantedAuthority> authorities =
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

                JwtUserPrincipal principal =
                        new JwtUserPrincipal(userId, email, "ROLE_USER", authorities);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("✅ SecurityContext 설정 완료: {}", email);
            } else {
                log.debug("⚠️ 유효하지 않은 토큰");
            }

        } catch (Exception e) {
            log.error("❌ JWT 필터 예외: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * ✅ 인증 예외 경로 설정
     */
    private boolean isExcludedPath(String path) {
        return path.startsWith("/actuator")
                || path.startsWith("/error")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/favicon")
                || path.startsWith("/css")
                || path.startsWith("/js")
                || path.startsWith("/images")
                || path.startsWith("/login")
                || path.startsWith("/oauth2")
                || path.startsWith("/api/public")
                || path.startsWith("/api/auth")
                || path.startsWith("/api/onboarding")
                || path.startsWith("/ws"); // WebSocket 경로 예외
    }
}
