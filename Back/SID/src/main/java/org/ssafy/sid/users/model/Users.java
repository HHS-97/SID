package org.ssafy.sid.users.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.ssafy.sid.profiles.dto.ProfileDeleteDTO;
import org.ssafy.sid.users.GenderEnum;
import org.ssafy.sid.users.StatusEnum;
import org.ssafy.sid.users.dto.UserDeleteDTO;
import org.ssafy.sid.users.dto.UserDetailDTO;
import org.ssafy.sid.users.dto.UserUpdateDTO;

import java.math.BigInteger;
import java.time.LocalDate;
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
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Users {
	@Id
	@Column(columnDefinition = "INT(11) UNSIGNED")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	// MySQL auto_increment 제약 때문에, 엔티티 id를 BigInteger로 쓰면서 @GeneratedValue(strategy = GenerationType.IDENTITY)를 붙이면 Incorrect column specifier 오류가 발생
	private long id;

	@Column(nullable = false, updatable = false, length = 32)
	// JPA에서는 String의 길이는 default로 varchar(255)
	private String email;

	@Column(length = 16)
	private String provider;

	@Column(length = 60)
	private String password;

	@Column(nullable = false, length = 30)
	private String name;

	@Column(length = 1)
	private char gender;

	@Column(length = 10, columnDefinition="CHAR(10)")
	private String birthDate;

	@Column(length = 16)
	private String phone;

	@Builder.Default
	@Column(nullable = false, length = 8)
	private String status = "N";

	@CreatedDate
	@Column(updatable = false, nullable = false, columnDefinition="CHAR(19)")
	private String createdAt;

	@LastModifiedDate
	@Column(nullable = false, columnDefinition="CHAR(19)")
	private String updatedAt;

	@Builder.Default
	@Column(nullable = false, length = 1)
	private char role = 'N';

	@Column(columnDefinition="CHAR(19)")
	private String penalty;

	@Builder.Default
	@Column(columnDefinition = "TINYINT(1)")
	private Boolean isDeleted = false;

	/*

	// 삭제 처리 여부 Y는 삭제됨, N은 삭제안됨
	@Builder.Default
	private char DEL_YN = 'N';

	// yyyy MM dd HH:mm:ss
	private String REG_DTTM;

	// 등록을 한 유저
	private String REG_USER_SEQ;

	// yyyy MM dd HH:mm:ss
	private String MOD_DTTM;

	// 1. system(인사과) -> cjflgownsms ruddn(퇴사)
	// 2. 당사자가 퇴사하는 경우
	// 3. admin(강제 퇴출, 강제 퇴장)
	private String MOD_USER_SEQ;

	 */

	@PrePersist
	public void onPrePersist(){
		this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		this.updatedAt = this.createdAt;
	}
	@PreUpdate
	public void onPreUpdate(){
		this.updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	public void update(UserUpdateDTO dto) {
		if (dto.getName() != null) {
			this.name = dto.getName();
		}
		if (dto.getGender() != null) {
			this.gender = dto.getGender().charAt(0);
		}
		if (dto.getBirthDate() != null) {
			this.birthDate = dto.getBirthDate();
		}
		if (dto.getPhone() != null) {
			this.phone = dto.getPhone();
		}
		if (dto.getPassword() != null) {
			this.password = dto.getPassword();
		}
	}

	public void delete(UserDeleteDTO dto) {
		if (Objects.equals(dto.getEmail(), this.email)) {
			this.isDeleted = true;
		}
	}
}
