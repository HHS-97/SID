package org.ssafy.sid.users.jwt;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.ssafy.sid.users.model.Users;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
//@Table(name = "refresh_tokens")
public class RefreshTokens {

	@Id
	@Column(columnDefinition = "INT(11) UNSIGNED")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", columnDefinition = "INT(11) UNSIGNED", nullable = false)
	private Users user;

	private String refreshToken;

	@Column(name = "refresh_expire", columnDefinition="CHAR(19)", nullable = false)
	private String refreshExpire;

	@Builder.Default
	@Column(length = 50)
	private String device = "Web";
}
