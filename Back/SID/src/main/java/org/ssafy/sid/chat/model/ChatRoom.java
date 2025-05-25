package org.ssafy.sid.chat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.ssafy.sid.profiles.dto.ProfileDeleteDTO;
import org.ssafy.sid.profiles.model.Profiles;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
@Setter
@Getter
@Table(name = "chat_room")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE chat_room SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class ChatRoom implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String roomName;

	private String senderNickname;           // 채팅방 생성자

	@Column(unique = true)
	private String roomId;

	private String receiverNickname;         // 채팅방 수신자

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@NotFound(action = NotFoundAction.IGNORE)
	@JoinColumn(name = "sender_id", columnDefinition = "INT(11) UNSIGNED", nullable = false, updatable = false)
	@JsonIgnore
	private Profiles sender;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@NotFound(action = NotFoundAction.IGNORE)
	@JoinColumn(name = "receiver_id", columnDefinition = "INT(11) UNSIGNED", nullable = false, updatable = false)
	@JsonIgnore
	private Profiles receiver;

	@CreatedDate
	@Column(name = "created_at")
	private String createdAt;

	// 참여자 활성 상태. 기본값은 true.
	@Column(columnDefinition = "TINYINT(1)")
	private boolean senderActive = true;
	@Column(columnDefinition = "TINYINT(1)")
	private boolean receiverActive = true;

	@Column(columnDefinition = "TINYINT(1)")
	private Boolean isDeleted = false;

	// 쪽지방 생성자
	public ChatRoom(Long id, String roomName, String senderNickname, String roomId, String receiverNickname, Profiles sender, Profiles receiver) {
		this.id = id;
		this.roomName = roomName;
		this.senderNickname = senderNickname;
		this.receiverNickname = receiverNickname;
		this.roomId = roomId;
		this.sender = sender;
		this.receiver = receiver;
		this.senderActive = true;
		this.receiverActive = true;
		this.isDeleted = false;
	}

	@PrePersist
	public void onPrePersist(){
		this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	public void delete() {
			this.isDeleted = true;
	}
}
