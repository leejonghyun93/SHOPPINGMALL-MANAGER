package org.kosa.shoppingmaillmanager.chat;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatDAO {
	
	void insertChatMessage(ChatMessageDTO message);
	List<ChatMessageDTO> getChatMessagesByBroadcastId(Long broadcastId);
}
