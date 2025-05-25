package org.ssafy.sid.users.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.ssafy.sid.users.GenderEnum;
import org.ssafy.sid.users.StatusEnum;
import org.ssafy.sid.users.model.Users;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UsersSaveDTO {
	@Pattern(regexp="^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])+[.][a-zA-Z]{2,3}$", message="이메일 주소 양식을 확인해주세요")
	// JPA에서는 String의 길이는 default로 varchar(255)
	private String email;

	@NotBlank(message = "비밀번호는 필수 입력 값입니다.")
//	테스트때에는 아래 검증은 주석처리
//	@Pattern(regexp="(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}",
//			message = "비밀번호는 영문 대,소문자와 숫자, 특수기호가 적어도 1개 이상씩 포함된 8자 ~ 20자의 비밀번호여야 합니다.")
	private String password;

	private String passwordConfirm;

	@NotBlank(message = "이름은 필수 입력 값입니다.")
	private String name;

	@NotNull(message = "성별은 필수 입력 값입니다.")
	private String gender;

	private String birthDate;

	private String phone;



	public UsersSaveDTO(Users user) {
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.name = user.getName();
		this.gender = String.valueOf(user.getGender());
		this.birthDate = user.getBirthDate();
		this.phone = user.getPhone();
	}

	public Users toEntity() {

		return Users.builder()
				.email(email)
				.password(password)
				.name(name)
				.gender(gender.charAt(0))
				.birthDate(birthDate)
				.phone(phone)
				.build();
	}

	public UsersSaveDTO toDTO(Users entity) {
		return UsersSaveDTO.builder()
				.email(entity.getEmail())
				.password(entity.getPassword())
				.name(entity.getName())
				.gender(String.valueOf(entity.getGender()))
				.birthDate(entity.getBirthDate())
				.phone(entity.getPhone())
				.build();
	}
}
