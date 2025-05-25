package org.ssafy.sid.lastprofiles.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.ssafy.sid.lastprofiles.dto.LastProfileDeleteDTO;
import org.ssafy.sid.lastprofiles.dto.LastProfileUpdateDTO;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.users.dto.UserDeleteDTO;
import org.ssafy.sid.users.model.Users;

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
public class LastProfiles {

	@Id
	@Column(columnDefinition = "INT(11) UNSIGNED")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	// MySQL auto_increment 제약 때문에, 엔티티 id를 BigInteger로 쓰면서 @GeneratedValue(strategy = GenerationType.IDENTITY)를 붙이면 Incorrect column specifier 오류가 발생
	private long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", columnDefinition = "INT(11) UNSIGNED", nullable = false)
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "profile_id", columnDefinition = "INT(11) UNSIGNED", nullable = false)
	private Profiles profile;

	@CreatedDate
	@Column(updatable = false, nullable = false, columnDefinition="CHAR(19)")
	private String createdAt;

	@Builder.Default
	@Column(columnDefinition = "TINYINT(1)")
	private Boolean isDeleted = false;

	@PrePersist
	public void onPrePersist(){
		this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	public void update(LastProfileUpdateDTO dto) {
		if (dto.getProfile() != null) {
			this.profile = dto.getProfile();
		}
	}

	public void delete(LastProfileDeleteDTO dto) {
		if (Objects.equals(dto.getUser(), this.user)) {
			this.isDeleted = true;
		}
	}
}
