package org.ssafy.sid.chat.service;

import org.ssafy.sid.chat.dto.ChatMessageDTO;
import org.ssafy.sid.chat.model.ChatMessage;

import java.util.List;

public interface ChatMessageService {
	void saveMessage(ChatMessageDTO chatMessageDto, ChatMessage.MessageType messageType);
	List<ChatMessageDTO> loadMessage(String roomId);
}
