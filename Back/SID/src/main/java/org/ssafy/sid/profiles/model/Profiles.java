package org.ssafy.sid.profiles.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.ssafy.sid.profiles.dto.ProfileDeleteDTO;
import org.ssafy.sid.profiles.dto.ProfileUpdateDTO;
import org.ssafy.sid.users.dto.UserUpdateDTO;
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
@SQLDelete(sql = "UPDATE profiles SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Profiles {
	@Id
	@Column(columnDefinition = "INT(11) UNSIGNED")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	// MySQL auto_increment 제약 때문에, 엔티티 id를 BigInteger로 쓰면서 @GeneratedValue(strategy = GenerationType.IDENTITY)를 붙이면 Incorrect column specifier 오류가 발생
	private long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", columnDefinition = "INT(11) UNSIGNED", nullable = false)
	private Users user;

	@Column(length = 16, nullable = false)
	@NotBlank
	private String nickname;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(length = 128)
	private String profileImage;

	@CreatedDate
	@Column(updatable = false, nullable = false, columnDefinition="CHAR(19)")
	private String createdAt;

	@LastModifiedDate
	@Column(nullable = false, columnDefinition="CHAR(19)")
	private String updatedAt;

	@Builder.Default
	@Column(columnDefinition = "TINYINT(1)")
	private Boolean isDeleted = false;

	@PrePersist
	public void onPrePersist(){
		this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		this.updatedAt = this.createdAt;
	}

	@PreUpdate
	public void onPreUpdate(){
		this.updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	public void update(ProfileUpdateDTO dto, String imageUrl) {
		if (dto.getNickname() != null && !dto.getNickname().equals(this.nickname) && !dto.getNickname().isBlank()) {
			this.nickname = dto.getNickname();
		}

		if (dto.getDescription() != null && !dto.getDescription().equals(this.description)) {
			this.description = dto.getDescription();
		}
		if (dto.getProfileImage() != null) {
			this.profileImage = imageUrl;
		}
	}

	public void delete(ProfileDeleteDTO dto) {
		if (Objects.equals(dto.getNickname(), this.nickname)) {
			this.isDeleted = true;
		}
	}
}
