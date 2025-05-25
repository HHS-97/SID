package org.ssafy.sid.oauth.service;

import org.ssafy.sid.oauth.dto.OAuth2AuthInfoDto;
import org.ssafy.sid.oauth.dto.OAuth2KakaoUserInfoDto;
import org.ssafy.sid.oauth.dto.OAuth2NaverUserInfoDto;
import org.ssafy.sid.oauth.dto.OAuth2SaveDTO;
import org.ssafy.sid.users.model.Users;

public interface OAuth2Service {

	OAuth2KakaoUserInfoDto kakaoLogin(OAuth2AuthInfoDto oAuth2AuthInfoDto);
	Users createUser(OAuth2SaveDTO oAuth2SaveDTO);
	OAuth2NaverUserInfoDto naverLogin(OAuth2AuthInfoDto oAuth2AuthInfoDto);
	
}
