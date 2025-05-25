package org.ssafy.sid.oauth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OAuth2AuthInfoDto {

	private String code;
	private String error;
	private String error_description;
	private String state;
	
	@Builder
	public OAuth2AuthInfoDto(String code, String error, String error_description, String state) {
		this.code = code;
		this.error = error;
		this.error_description = error_description;
		this.state = state;
	}
	
}
