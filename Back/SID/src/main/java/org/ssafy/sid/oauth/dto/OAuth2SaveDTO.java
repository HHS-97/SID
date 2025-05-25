package org.ssafy.sid.oauth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.ssafy.sid.users.model.Users;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2SaveDTO {
	@Pattern(regexp = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])+[.][a-zA-Z]{2,3}$", message = "이메일 주소 양식을 확인해주세요")
	// JPA에서는 String의 길이는 default로 varchar(255)
	private String email;

	@NotBlank(message = "이름은 필수 입력 값입니다.")
	private String name;

	@NotNull(message = "성별은 필수 입력 값입니다.")
	private String gender;

	private String birthDate;

	private String phone;

	private String provider;

	public Users toEntity() {

		return Users.builder()
				.email(email)
				.name(name)
				.gender(gender.charAt(0))
				.birthDate(birthDate)
				.phone(phone)
				.provider(provider)
				.build();
	}

	public org.ssafy.sid.users.dto.UsersSaveDTO toDTO(Users entity) {
		return org.ssafy.sid.users.dto.UsersSaveDTO.builder()
				.email(entity.getEmail())
				.name(entity.getName())
				.gender(String.valueOf(entity.getGender()))
				.birthDate(entity.getBirthDate())
				.phone(entity.getPhone())
				.build();
	}
}
