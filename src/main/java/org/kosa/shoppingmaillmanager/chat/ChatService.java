package org.kosa.shoppingmaillmanager.chat;

import java.util.List;

import org.kosa.shoppingmaillmanager.host.product.dto.BroadcastStatusDTO;
import org.kosa.shoppingmaillmanager.member.BroadcastRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

	private final ChatDAO chatDAO;
	
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
}
