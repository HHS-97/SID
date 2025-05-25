package org.ssafy.sid.fcm.model;

import java.time.LocalDateTime;

import org.ssafy.sid.users.model.Users;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fcm_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "fcm_token_id", columnDefinition = "INT(11) UNSIGNED")
    private Long id;
	
	private String fcmToken;
	
	private LocalDateTime lastUserAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
    private Users user;
	
	@Builder
    public FcmToken(String fcmToken, LocalDateTime lastUserAt, Users user) {
        this.fcmToken = fcmToken;
        this.lastUserAt = lastUserAt;
        this.user = user;
    }

	public void updateTokenLastUserAt() {
		this.lastUserAt = LocalDateTime.now();
	}
}
