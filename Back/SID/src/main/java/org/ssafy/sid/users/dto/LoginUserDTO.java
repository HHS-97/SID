package org.ssafy.sid.users.dto;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.users.model.Users;

@Data
public class LoginUserDTO {
	private long user_id;
	private String email;
	private String accessToken;
	private String refreshToken;
	private Profiles lastProfile;
}
