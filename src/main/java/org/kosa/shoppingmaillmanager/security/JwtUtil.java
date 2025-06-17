package org.kosa.shoppingmaillmanager.security;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    // application.yml 또는 application.properties에서 주입받음
    @Value("${jwt.secret}")
    private String secretKey;

    // 토큰 만료 시간: 1시간
    private final long expireTime = 1000 * 60 * 60;

    /**
     * Base64 인코딩된 secretKey를 디코딩하여 SecretKey 객체 반환
     */
    private SecretKey getSigningKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    /**
     * JWT 토큰 생성
     * @param userId 사용자 ID
     * @return 생성된 JWT 문자열
     */
    public String generateToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireTime);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 토큰에서 userId(subject) 추출
     * @param token 클라이언트가 보낸 JWT
     * @return userId
     */
    public String validateTokenAndGetUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * JWT 토큰이 유효한지 검증
     * @param token 클라이언트가 보낸 JWT
     * @return 유효 여부
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}