package org.kosa.shoppingmaillmanager.host.broadcast;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
* Redis를 사용하는 이유:
* 시청자 수가 빠르게 변할 때마다 DB에 UPDATE를 날리면 성능이 떨어짐
* Redis는 메모리 기반이라 매우 빠르게 read/write 가능함 (ms 단위)
* 실시간 시청자 수를 저장하고, 방송 종료 시에만 DB로 저장하면 성능 부담이 없음
**/

@Service
@RequiredArgsConstructor
public class ViewerRedisService {
	// Redis에서 문자열(String) 기반으로 데이터를 다룰 수 있는 템플릿
    private final StringRedisTemplate redisTemplate;

    // Redis 키의 접두사 (예: broadcast:viewers:47 같은 형식으로 사용됨)
    private static final String PREFIX = "broadcast:viewers:";

    /**
     * 시청자가 입장할 때 호출 → Redis에서 시청자 수 1 증가
     * 예: broadcast:viewers:47 → 5 → 6
     */
    public void increase(int broadcastId) {
        redisTemplate.opsForValue().increment(PREFIX + broadcastId);
    }

    /**
     * 시청자가 퇴장할 때 호출 → Redis에서 시청자 수 1 감소
     * 예: broadcast:viewers:47 → 6 → 5
     */
    public void decrease(int broadcastId) {
        redisTemplate.opsForValue().decrement(PREFIX + broadcastId);
    }

    /**
     * 현재 Redis에 저장된 시청자 수를 가져오는 메서드
     * - 값이 없으면 0으로 처리 (null 방지)
     * 예: broadcast:viewers:47 → "5" → long 5
     */
    public long getCount(int broadcastId) {
        String val = redisTemplate.opsForValue().get(PREFIX + broadcastId);
        return val == null ? 0 : Long.parseLong(val);  // null이면 0, 아니면 숫자로 변환
    }

    /**
     * 방송이 끝났을 때 호출 → Redis에서 해당 키 제거
     * - 메모리 누수 방지
     */
    public void remove(int broadcastId) {
        redisTemplate.delete(PREFIX + broadcastId);
    }
}
