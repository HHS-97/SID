package org.ssafy.sid.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ssafy.sid.chat.model.ChatMessage;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageDTO {
	private String sender;
	private String roomId;
	private String message;
	private String sentTime;
	private ChatMessage.MessageType type;

	// 대화 조회
	public ChatMessageDTO(ChatMessage chatMessage) {
		this.sender = chatMessage.getSender();
		this.roomId = chatMessage.getRoomId();
		this.message = chatMessage.getMessage();
		this.sentTime = String.valueOf(chatMessage.getCreatedAt());
		this.type = chatMessage.getType();
	}

	public static ChatMessageDTO fromEntity(ChatMessage chatMessage) {
		ChatMessageDTO dto = new ChatMessageDTO();
		dto.setSender(chatMessage.getSender());
		dto.setRoomId(chatMessage.getRoomId());
		dto.setMessage(chatMessage.getMessage());
		dto.setType(chatMessage.getType());
		return dto;
	}
}
