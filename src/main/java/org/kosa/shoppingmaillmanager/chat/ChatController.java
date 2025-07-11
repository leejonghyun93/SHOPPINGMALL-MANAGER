package org.kosa.shoppingmaillmanager.chat;

import java.util.List;
import java.util.Map;

import org.kosa.shoppingmaillmanager.chat.util.ChatFilterUtil;
import org.kosa.shoppingmaillmanager.host.product.dto.BroadcastStatusDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Controller
@Slf4j
public class ChatController {
	
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatService chatService;
	private final ChatSessionManager chatSessionManager;
	
	@MessageMapping("/sendMessage")
	public void sendMessage(ChatMessageDTO message) {
		
		if (message.getType() == null || message.getType().isBlank()) {
            message.setType("text");
        }
		
		//욕설 필터링
		message.setText(ChatFilterUtil.filterBadWords(message.getText()));
		
		chatService.saveChatMessage(message);
		
		//전체 채팅방에 뿌림
	    messagingTemplate.convertAndSend("/topic/public", message);
	}
	
	@GetMapping("/chat/history/{broadcastId}")
	public ResponseEntity<List<ChatMessageDTO>> getChatHistory(@PathVariable Long broadcastId) {
		return ResponseEntity.ok(chatService.getHistoryByBroadcastId(broadcastId));
	}
	
	@GetMapping("/broadcasts/{broadcastId}/status")
	public ResponseEntity<BroadcastStatusDTO> getBroadcastStatus(@PathVariable Long broadcastId) {
		BroadcastStatusDTO dto = chatService.getBroadcastStatus(broadcastId);
		return ResponseEntity.ok(dto);
	}
	
	@GetMapping("/chat/participants/{broadcastId}")
    public ResponseEntity<Map<String, Object>> getParticipantCount(@PathVariable Long broadcastId) {
        int count = chatSessionManager.getParticipantCount(broadcastId);
        return ResponseEntity.ok(Map.of("count", count));
    }
	
	@PostMapping("/chat/disconnect/{broadcastId}")
	@ResponseBody
	public ResponseEntity<Void> disconnectManually(
	        @PathVariable Long broadcastId,
	        @RequestParam("id") String id) {

	    chatSessionManager.removeSessionManually(broadcastId, id);
	    return ResponseEntity.ok().build();
	}
}




/*
 * @MessageMapping("/sendMessage") 클라이언트가 /app/sendMessage 로 보내면 이 메서드가 받음
 * @SendTo 모든 구독자에게 메세지를 뿌릴 경로
 * return message 다시 브로커에게 전달
 * 
 * 
 * 
 */
