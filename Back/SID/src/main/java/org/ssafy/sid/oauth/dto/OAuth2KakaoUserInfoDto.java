package org.ssafy.sid.oauth.dto;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2KakaoUserInfoDto {
	
	private String id;
	private int statusCode;
	private String nickname;
	private String email;
	private String name;
	private String gender;
//	private String age_range;
	private String birthday;
	private String birthyear;
	private String number;
	private String provider;
	
	@Builder
	public OAuth2KakaoUserInfoDto(String id, int statusCode, String nickname, String email, String name, String gender,
			String birthday, String birthyear, String number, String provider) {
		super();
		this.id = id;
		this.statusCode = statusCode;
		this.nickname = nickname;
		this.email = email;
		this.name = name;
		this.gender = gender;
//		this.age_range = age_range;
		this.birthday = birthday;
		this.birthyear = birthyear;
		this.number = number;
		this.provider = provider;
	}

	
}
