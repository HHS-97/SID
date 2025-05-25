package org.ssafy.sid.withdraws.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.ssafy.sid.users.model.Users;
import org.ssafy.sid.withdraws.dto.WithdrawDeleteDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE withdraws SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Withdraws {
	@Id
	@Column(columnDefinition = "INT(11) UNSIGNED")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", columnDefinition = "INT(11) UNSIGNED", nullable = false, unique = true)
	private Users user;

	@Column(columnDefinition = "TEXT")
	private String withdrawReason;

	@CreatedDate
	@Column(updatable = false, nullable = false, columnDefinition="CHAR(19)")
	private String requestedAt;

	@Builder.Default
	@Column(columnDefinition = "TINYINT(1)")
	private Boolean isDeleted = false;

	@PrePersist
	public void onPrePersist(){
		this.requestedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		this.requestedAt = this.requestedAt;
	}

	public void delete(WithdrawDeleteDTO dto) {
		if (Objects.equals(dto.getEmail(), this.user.getEmail())) {
			this.isDeleted = true;
		}
	}

	public void deleteCancel(WithdrawDeleteDTO dto) {
		if (Objects.equals(dto.getEmail(), this.user.getEmail())) {
			this.isDeleted = false;
		}
	}
}
