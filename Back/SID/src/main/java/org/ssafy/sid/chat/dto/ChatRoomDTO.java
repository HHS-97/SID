package org.ssafy.sid.chat.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ssafy.sid.profiles.model.Profiles;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
// Redis 에 저장되는 객체들이 직렬화가 가능하도록
public class ChatRoomDTO implements Serializable {
	private static final long serialVersionUID = 6494678977089006639L;      // 역직렬화 위한 serialVersionUID 세팅
	private Long id;
	private String roomName;
	private String roomId;
	private String senderNickname;     // 메시지 송신자
	private String receiverNickname;   // 메시지 수신자
	@JsonIgnore
	private Profiles sender;
	@JsonIgnore
	private Profiles receiver;

	// 쪽지방 생성
	public static ChatRoomDTO create(Profiles sender, Profiles receiver) {
		ChatRoomDTO chatRoomDTO = new ChatRoomDTO();
		chatRoomDTO.roomName = sender.getNickname();
		chatRoomDTO.roomId = UUID.randomUUID().toString();
		chatRoomDTO.senderNickname = sender.getNickname();
		chatRoomDTO.receiverNickname = receiver.getNickname();
		chatRoomDTO.sender = sender;
		chatRoomDTO.receiver = receiver;

		return chatRoomDTO;
	}

	// 사용자 관련 쪽지방 선택 조회
	public ChatRoomDTO(Long id, String roomName, String roomId, String senderNickname, String receiverNickname, Profiles sender, Profiles receiver) {
		this.id = id;
		this.roomName = roomName;
		this.roomId = roomId;
		this.sender = sender;
		this.receiver = receiver;
		this.senderNickname = senderNickname;
		this.receiverNickname = receiverNickname;
	}
}
