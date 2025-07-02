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

    // application.yml 또는 properties에서 주입받는 JWT 시크릿 키 (Base64 인코딩 상태)
    @Value("${jwt.secret}")
    private String secretKey;

    // JWT 토큰 유효 시간 (1시간)
    private final long expireTime = 1000 * 60 * 60; // 1시간
    private final long REFRESH_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7; // 7일

    /**
     * Base64로 인코딩된 시크릿 키를 디코딩하여 HMAC-SHA 서명을 위한 SecretKey 객체로 변환
     */
    private SecretKey getSigningKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey); // 디코딩
        return Keys.hmacShaKeyFor(decodedKey); // SecretKey 생성
    }

    /**
     * JWT 토큰 생성 메서드
     * @param userId JWT에 담을 사용자 ID (subject)
     * @return 생성된 JWT 문자열
     */
    public String generateToken(String userId) {
        Date now = new Date(); // 현재 시간
        Date expiry = new Date(now.getTime() + expireTime); // 만료 시간 계산

        // JWT 빌더를 사용해 토큰 생성
        return Jwts.builder()
                .setSubject(userId)               // 사용자 ID를 subject로 설정
                .setIssuedAt(now)                 // 발급 시간
                .setExpiration(expiry)            // 만료 시간
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 서명
                .compact();                       // 최종 JWT 문자열 생성
    }
    
    /**
     * JWT Refresh 토큰 생성 메서드
     * 로그인 연장 버튼 클릭 시 로그인 시간 연장
     * @param userId JWT에 담을 사용자 ID (subject)
     * @return 생성된 JWT 문자열
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date(); // 현재 시간
        Date expiry = new Date(now.getTime() + REFRESH_EXPIRE_TIME); // 만료 시간 계산

        // JWT 빌더를 사용해 토큰 생성
        return Jwts.builder()
                .setSubject(userId)               // 사용자 ID를 subject로 설정
                .setIssuedAt(now)                 // 발급 시간
                .setExpiration(expiry)            // 만료 시간
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 서명
                .compact();                       // 최종 JWT 문자열 생성
    }

    /**
     * 토큰에서 사용자 ID(subject) 추출
     * @param token 클라이언트로부터 받은 JWT
     * @return subject (userId)
     */
    public String validateTokenAndGetUserId(String token) {
        // 파싱 후 클레임에서 subject(userId) 추출
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // 서명 키 설정
                .build()
                .parseClaimsJws(token)          // 토큰 파싱
                .getBody()
                .getSubject();                  // subject 반환
    }

    /**
     * JWT 유효성 검증 메서드
     * @param token 검사할 JWT 문자열
     * @return 유효한 경우 true, 예외 발생 시 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token); // 파싱 성공 시 유효함
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 유효하지 않은 토큰이거나 서명 키 불일치
            return false;
        }
    }

    /**
     * JWT에서 만료 시간 추출
     * 이미 만들어진 토큰을 디코딩해서 만료시간을 꺼내는 함수
     */
	public Date getTokenExpiration(String newAccessToken) {
		return Jwts.parserBuilder() // jwt 파서 만듦
	               .setSigningKey(getSigningKey()) // 서명 검증용 시크릿키 설정
	               .build()
	               .parseClaimsJws(newAccessToken) // JWT 파싱 (예외 발생 가능)
	               .getBody() // Claims (payload 부분)
	               .getExpiration(); // exp 필드 -> Data 객체로 반환
	}

}