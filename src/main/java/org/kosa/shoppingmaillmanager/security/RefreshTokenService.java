package org.kosa.shoppingmaillmanager.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    // 유저ID → RefreshToken 을 저장하는 맵
    // 실제 운영에서는 이 자리에 RedisTemplate 또는 DB를 사용하는 것이 안전함
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();
    // → Thread-safe한 Map 구현체로, 다중 사용자 환경에서도 동시성 문제를 방지함

    /**
     * RefreshToken 저장
     * 사용자가 로그인하거나 리프레시 토큰이 새로 발급될 때, 해당 토큰을 저장한다.
     * @param userId 사용자 식별자 (예: email, username, UUID 등)
     * @param refreshToken 발급된 Refresh Token
     */
    public void save(String userId, String refreshToken) {
        tokenStore.put(userId, refreshToken); // 기존 값을 덮어씀 (1명당 1개 유지)
    }

    /**
     * RefreshToken 유효성 검증
     * 요청으로 들어온 토큰이 저장된 토큰과 동일한지 확인
     * @param userId 사용자 ID
     * @param refreshToken 클라이언트가 전달한 리프레시 토큰
     * @return 저장된 토큰과 일치하면 true, 아니면 false
     */
    public boolean isValid(String userId, String refreshToken) {
        return refreshToken.equals(tokenStore.get(userId));
    }

    /**
     * 로그아웃 또는 토큰 만료 시 RefreshToken 제거
     * @param userId 사용자 ID
     */
    public void delete(String userId) {
        tokenStore.remove(userId); // 토큰 삭제 → 연장 불가능하게 됨
    }
}
