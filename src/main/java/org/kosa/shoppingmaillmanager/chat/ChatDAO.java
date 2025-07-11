package org.kosa.shoppingmaillmanager.chat;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.kosa.shoppingmaillmanager.host.product.dto.BroadcastStatusDTO;

import io.lettuce.core.dynamic.annotation.Param;

@Mapper
public interface ChatDAO {
	
	void insertChatMessage(ChatMessageDTO message);
	List<ChatMessageDTO> getChatMessagesByBroadcastId(Long broadcastId);
	String getBroadcasterIdByBroadcastId(Long broadcastId);
	BroadcastStatusDTO getBroadcastStatusById(@Param("broadcastId") Long broadcastId);
}
