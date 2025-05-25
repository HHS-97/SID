package org.ssafy.sid.oauth.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2NaverUserInfoDto {

	private String resultcode;
	private String message;
	private NaverUserResponse response;
	
	@Getter
	@Setter
	@Builder
	public static class NaverUserResponse {
		private String id;
		private String name;
		private String email;
		private String gender;
		private String birthday;
		private String birthyear;
		private String mobile;
	}

	@Builder
	public OAuth2NaverUserInfoDto(String resultcode, String message, NaverUserResponse response) {
		super();
		this.resultcode = resultcode;
		this.message = message;
		this.response = response;
	}
	
}
