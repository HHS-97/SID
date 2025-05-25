package org.ssafy.sid.users.dto;

import jakarta.persistence.Column;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.ssafy.sid.users.model.Users;

@Getter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class UserDetailDTO {
	private String email;
	private String provider;
	private String name;
	private char gender;
	private String birthDate;
	private String phone;
	private String status;
	private char role;
	private String penalty;

	public UserDetailDTO toUserDetailDTO(Users user) {
		return UserDetailDTO.builder()
				.email(user.getEmail())
				.name(user.getName())
				.gender(user.getGender())
				.birthDate(user.getBirthDate())
				.phone(user.getPhone())
				.provider(user.getProvider())
				.status(user.getStatus())
				.role(user.getRole())
				.penalty(user.getPenalty())
				.build();
	}
}
