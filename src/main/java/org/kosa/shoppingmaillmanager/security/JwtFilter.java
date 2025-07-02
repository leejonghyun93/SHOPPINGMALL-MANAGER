package org.kosa.shoppingmaillmanager.security;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JwtFilter 클래스는 JWT 생성, 검증, 인증 처리 기능을 통합한 필터 클래스입니다.
 * Spring Security의 OncePerRequestFilter를 상속받아 매 요청마다 JWT 검사 및 인증을 수행합니다.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {


private final JwtUtil jwtUtil;

public JwtFilter(JwtUtil jwtUtil) {
this.jwtUtil = jwtUtil;
}

    /**
     * Spring Security의 필터 체인에서 매 요청마다 실행되는 메서드
     * JWT가 유효하면 사용자 인증 처리를 수행
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 요청 헤더에서 Authorization 헤더를 꺼냄 (예: "Bearer eyJ...")
        String authHeader = request.getHeader("Authorization");

        // 헤더가 존재하고 "Bearer "로 시작하는 경우에만 처리
        if (authHeader != null && authHeader.startsWith("Bearer")) {
            // "Bearer " 문자열 제거 → 순수 토큰만 추출
            String token = authHeader.substring(7);

            try {
                // 토큰이 유효한 경우
                if (jwtUtil.validateToken(token)) {
                    // 토큰에서 사용자 ID 추출
                    String userId = jwtUtil.validateTokenAndGetUserId(token);

                    // 인증 객체 생성 (권한은 없음 → null)
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, null);

                    // 인증 객체에 요청 정보 추가 (IP, 세션 등)
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // SecurityContext에 등록 → 이후 요청은 인증된 사용자로 인식
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    request.setAttribute("userId", userId);
                }

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Token 만료");
                return;
            } catch (io.jsonwebtoken.SignatureException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "서명이 일치하지 않음");
                return;
            } catch (io.jsonwebtoken.MalformedJwtException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "잘못된 JWT 형식");
                return;
            } catch (JwtException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 JWT");
                return;
            }
        } else {

         System.out.println("⚠️ Authorization 헤더 없음 또는 Bearer 형식 아님");

        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
