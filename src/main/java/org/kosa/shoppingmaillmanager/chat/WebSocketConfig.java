package org.kosa.shoppingmaillmanager.chat;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectEvent;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic"); //구독할 prefix
		config.setApplicationDestinationPrefixes("/app"); //발행할 prefix
	}
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws-chat") //웹소켓 연결할 URL
				.setAllowedOriginPatterns("*") // CORS 허용 (개발용)
				.withSockJS(); // SockJS fallback 지원
	}
	
	@EventListener
	public void handleConnect(SessionConnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		
		String uuid = accessor.getFirstNativeHeader("uuid");
		String jwt = accessor.getFirstNativeHeader("Authorization");
		String broadcastId = accessor.getFirstNativeHeader("broadcastId");
		
	}
}
