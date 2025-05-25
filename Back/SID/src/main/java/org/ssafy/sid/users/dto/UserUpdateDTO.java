package org.ssafy.sid.users.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class UserUpdateDTO {
	private String name;
	private String gender;
	private String birthDate;
	private String phone;
	private String password;
}
