package org.ssafy.sid.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;

@Data
public class LoginDTO {

	@NotBlank(message = "이메일을 입력해주세요")
	@Pattern(regexp="^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])+[.][a-zA-Z]{2,3}$", message="이메일 주소 양식을 확인해주세요")
	// JPA에서는 String의 길이는 default로 varchar(255)
	private String email;

	@NotBlank(message = "비밀번호를 입력해주세요")
	private String password;

	private boolean rememberMe;
}
