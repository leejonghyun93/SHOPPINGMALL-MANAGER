package org.kosa.shoppingmaillmanager.chat;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.kosa.shoppingmaillmanager.host.product.dto.BroadcastStatusDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

	private final ChatDAO chatDAO;

	private final RedisTemplate<String, String> redisTemplate;

	private final SimpMessagingTemplate messagingTemplate;

	private final ScheduledExecutorService scheduler =
		    Executors.newSingleThreadScheduledExecutor(r -> {
		        Thread t = new Thread(r);
		        t.setName("ChatBanScheduler");
		        return t;
		    });

	public void saveChatMessage(ChatMessageDTO message) {

		chatDAO.insertChatMessage(message);
	}

	public List<ChatMessageDTO> getHistoryByBroadcastId(Long broadcastId) {
		// 1. 채팅 목록 조회
		List<ChatMessageDTO> messages = chatDAO.getChatMessagesByBroadcastId(broadcastId);

		// 2. 방송 호스트 ID 조회
		String broadcasterId = chatDAO.getBroadcasterIdByBroadcastId(broadcastId);
		log.info("방송 [{}]의 호스트 ID: {}", broadcastId, broadcasterId);

		// 3. 메시지 중 호스트의 메시지는 "관리자"로 표시
		for (ChatMessageDTO msg : messages) {
			if (broadcasterId != null && broadcasterId.equals(msg.getUserId())) {
				msg.setFrom("관리자");
			}
		}

		return messages;
	}

	public BroadcastStatusDTO getBroadcastStatus(Long broadcastId) {
		return chatDAO.getBroadcastStatusById(broadcastId);
	}

	public void banUser(Long broadcastId, String userId, long durationSeconds) {
		log.info("🔒 채팅 금지 요청 → broadcastId={}, userId={}, durationSeconds={}", broadcastId, userId, durationSeconds);

		try {
			// 1. Redis에 금지 상태 저장 (TTL 설정)
			String key = "chat:ban:" + broadcastId + ":" + userId;
			if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
			    throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 금지된 사용자입니다.");
			}
			
			redisTemplate.opsForValue().set(key, "banned", Duration.ofSeconds(durationSeconds));
			log.info("✅ Redis에 금지 키 [{}] 등록 완료", key);

			// 2. 프론트에 즉시 '금지됨' 메시지 전송
			messagingTemplate.convertAndSend("/topic/ban/" + userId,
					Map.of("banned", true, "duration", durationSeconds));
			log.info("📡 STOMP 알림 발송 완료 → /topic/ban/{}", userId);

			// 3. 지정 시간 뒤 '금지 해제' 메시지 전송 (스레드 재사용 방식)
			scheduler.schedule(() -> {
				try {
					messagingTemplate.convertAndSend("/topic/ban/" + userId, Map.of("banned", false));
					log.info("🔓 자동 해제 메시지 전송 완료 → /topic/ban/{}", userId);
				} catch (Exception e) {
					log.error("❌ 자동 해제 메시지 전송 실패", e);
				}
			}, durationSeconds, TimeUnit.SECONDS);

		} catch (Exception e) {
			log.error("❌ 채팅 금지 처리 중 예외 발생", e);
			throw e; // 예외 다시 던져서 500 응답 유지
		}
	}

	public boolean isUserBanned(Long broadcastId, String userId) {
		String key = "chat:ban:" + broadcastId + ":" + userId;
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}
	
	@PreDestroy
	public void shutdownScheduler() {
	    log.info("🛑 채팅 스케줄러 종료 시도 중...");
	    scheduler.shutdown();
	    try {
	        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
	            log.warn("⚠️ 스케줄러 종료 대기 초과. 강제 종료 시도.");
	            scheduler.shutdownNow();
	        } else {
	            log.info("✅ 채팅 스케줄러 정상 종료됨.");
	        }
	    } catch (InterruptedException e) {
	        log.error("❌ 스케줄러 종료 중 인터럽트 발생", e);
	        scheduler.shutdownNow();
	        Thread.currentThread().interrupt(); // 현재 스레드 상태 복구
	    }
	}
}
