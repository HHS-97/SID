package org.ssafy.sid.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ssafy.sid.chat.model.ChatRoom;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRoomResponseDTO {
	private Long id;
	private String roomName;
	private String senderNickname;
	private String roomId;
	private String receiverNickname;
	private String message;
	private String createdAt;

	// 쪽지방 생성
	public ChatRoomResponseDTO(ChatRoom chatRoom) {
		this.id = chatRoom.getId();
		this.roomName = chatRoom.getRoomName();
		this.senderNickname = chatRoom.getSenderNickname();
		this.roomId = chatRoom.getRoomId();
		this.receiverNickname = chatRoom.getReceiverNickname();
		this.createdAt = chatRoom.getCreatedAt();
	}

	// 사용자 관련 쪽지방 전체 조회
	public ChatRoomResponseDTO(Long id, String roomName, String roomId, String senderNickname, String receiverNickname) {
		this.id = id;
		this.roomName = roomName;
		this.roomId = roomId;
		this.senderNickname = senderNickname;
		this.receiverNickname = receiverNickname;
	}

	public ChatRoomResponseDTO(String roomId) {
		this.roomId = roomId;
	}

	public void setLatestMessageContent(String message) {
		this.message = message;
	}

	public void setLatestMessageCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
}
