package org.ssafy.sid.fcm.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.users.model.Users;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id", columnDefinition = "INT(11) UNSIGNED")
    private Long id;
	
	private String title;
	
	private String body;
	
	@Builder.Default
	@Column(columnDefinition = "TINYINT(1)")
	private Boolean isRead = false;
	
	@CreatedDate
	@Column(updatable = false, nullable = false, columnDefinition="CHAR(19)")
	private String createdAt;
	
	private String type; // post 인지 이런거 파악하기 위한 형태
	
	private Long referenceId;
	
	//RECEIVER_ID -> 글을 받는 사람들의 목록
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id", nullable = false)
    private Profiles receiver;
	
	//sender Id -> 글을 작성한 사람이다
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id", nullable = false)
    private Profiles sender;
	
	private String image;
	
	private String room;
	
	@PrePersist
	public void onPrePersist(){
		this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}
	// 글을 받아야 하는 사람들에 대한 FCM_TOKEN을 받아야 함
	public void setRead(boolean isRead) {
	    this.isRead = isRead;
	}
}
