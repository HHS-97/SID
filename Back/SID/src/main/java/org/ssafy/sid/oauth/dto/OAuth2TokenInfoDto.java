package org.ssafy.sid.oauth.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2TokenInfoDto {
	
	private String accessToken;
	private String refreshToken;
	private String tokenType;
	private String expiresIn;
	private String error;
	private String errorDescription;
	
	@Builder
	public OAuth2TokenInfoDto(String accessToken, String refreshToken, String tokenType, String expiresIn, String error,
			String errorDescription) {
		super();
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.tokenType = tokenType;
		this.expiresIn = expiresIn;
		this.error = error;
		this.errorDescription = errorDescription;
	}
	
	
}
