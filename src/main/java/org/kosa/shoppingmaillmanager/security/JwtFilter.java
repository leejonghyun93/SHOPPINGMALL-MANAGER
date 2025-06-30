package org.kosa.shoppingmaillmanager.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;


public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // JwtUtil을 주입받는 생성자
    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 매 요청마다 실행되는 필터 메서드
     * JWT 토큰이 Authorization 헤더에 존재하면 검증 후 Spring Security 인증 처리
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 요청 헤더에서 Authorization 값을 꺼냄 (예: "Bearer eyJhbGciOi...")
        String authHeader = request.getHeader("Authorization");

        // 헤더가 null이 아니고 "Bearer "로 시작할 경우만 처리
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // "Bearer " 문자열을 제거하여 실제 토큰 값만 추출
            String token = authHeader.substring(7);

            try {
                // 토큰이 유효한지 검증
                if (jwtUtil.validateToken(token)) {
                    // 유효하다면 토큰에서 사용자 ID 추출
                    String userId = jwtUtil.validateTokenAndGetUserId(token);

                    // Spring Security 인증 객체 생성 (권한은 null로 비워둠)
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, null);

                    // 현재 요청 정보를 authentication 객체에 설정 (IP, 세션 등)
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 인증 객체를 SecurityContext에 등록 → 이후 컨트롤러에서 인증된 사용자로 간주
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (JwtException e) {
                // 유효하지 않은 토큰일 경우 무시하고 필터 체인 계속 진행
                // 예외를 던지지 않으면 다음 필터로 요청이 넘어감 (401 처리 안 함)
            }
        }

        // 다음 필터로 요청 계속 전달
        filterChain.doFilter(request, response);
    }
}