package org.kosa.shoppingmaillmanager.chat;

import org.kosa.shoppingmaillmanager.security.JwtUtil;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

	private final ChatSessionManager sessionManager;
	private final JwtUtil jwtUtil;
	
	@EventListener
	public void handleConnect(SessionConnectEvent event) {
	    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

	    String jwt = accessor.getFirstNativeHeader("Authorization");
	    String uuid = accessor.getFirstNativeHeader("uuid");
	    String broadcastId = accessor.getFirstNativeHeader("broadcastId");
	    String sessionId = accessor.getSessionId();

	    if (broadcastId == null || sessionId == null) return;

	    String userId = null;
	    if (jwt != null && jwt.startsWith("Bearer ")) {
	        try {
	            String token = jwt.substring(7);
	            userId = jwtUtil.validateTokenAndGetUserId(token); // ✅ 여기!
	        } catch (JwtException e) {
	            log.warn("❌ WebSocket 연결 시 토큰 오류: {}", e.getMessage());
	        }
	    }

	    String id = (userId != null) ? userId : uuid;

	    if (id != null) {
	        sessionManager.addSession(Long.parseLong(broadcastId), id, sessionId);
	        log.info("🟢 WebSocket 연결됨: ID={}, 방송ID={}, 세션ID={}", id, broadcastId, sessionId);
	    }
	}
	
	@EventListener
	public void handleDisconnect(SessionDisconnectEvent event) {
	    String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();

	    // Redis에서 sessionId 기반으로 userId/uuid, broadcastId 조회 후 제거
	    sessionManager.removeSessionBySessionId(sessionId);

	    log.info("🔴 WebSocket 연결 해제됨: 세션ID={}", sessionId);
	}
}
