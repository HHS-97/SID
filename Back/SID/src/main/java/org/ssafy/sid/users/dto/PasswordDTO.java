package org.ssafy.sid.users.dto;

import lombok.Data;

@Data
public class PasswordDTO {
	private String currentPassword;
	private String newPassword1;
	private String newPassword2;
}
