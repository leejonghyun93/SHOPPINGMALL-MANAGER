package org.kosa.shoppingmaillmanager.member;

import io.jsonwebtoken.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        System.out.println("🪪 Authorization 헤더: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("🔍 추출된 토큰: " + token);

            try {
                Claims claims = Jwts.parser()
                    .setSigningKey(Base64.getDecoder().decode(secretKey))
                    .parseClaimsJws(token)
                    .getBody();

                String userId = claims.getSubject(); // 기본적으로 sub

                // 추가로 userId 또는 user_id 클레임에 값이 있으면 우선적으로 사용
                if (claims.get("userId") != null) {
                    userId = claims.get("userId", String.class);
                } else if (claims.get("user_id") != null) {
                    userId = claims.get("user_id", String.class);
                }

                System.out.println("✅ JWT 파싱 성공. userId: " + userId);

                if (userId != null) {
                    request.setAttribute("userId", userId);
                }
            } catch (JwtException e) {
                System.out.println("❌ JWT 파싱 실패: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }
        } else {
            System.out.println("⚠️ Authorization 헤더 없음 또는 Bearer 형식 아님");
        }

        filterChain.doFilter(request, response);
    }
}
