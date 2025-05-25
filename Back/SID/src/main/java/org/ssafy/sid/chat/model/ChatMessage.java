package org.ssafy.sid.chat.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.ssafy.sid.profiles.model.Profiles;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Getter
@ToString
@Table(name = "chat_message")
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "메시지 타입은 필수 값입니다.")
	private MessageType type;

	@Column(name = "sender")
	private String sender;

	// roomId 필드를 chat_room_id 컬럼과 매핑하여, INSERT 시 해당 값이 들어가도록 함
	@Column(name = "room_id")
	private String roomId;

	@Column(name = "receiver")
	private String receiver;

	@Column(name = "message")
	private String message;

	@CreatedDate
	@Column(name = "created_at")
	private String createdAt;

	@PrePersist
	public void onPrePersist(){
		this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	// 대화 저장 생성자
	public ChatMessage(String sender, String roomId, String message, MessageType type) {
		this.sender = sender;
		this.roomId = roomId;
		this.message = message;
		this.type = type;
	}

	public enum MessageType {
		CHAT,
		JOIN,
		LEAVE
	}
}
