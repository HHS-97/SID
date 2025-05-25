package org.ssafy.sid.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client.provider")
public class OAuth2ProviderProperties {
	
	private ProviderConfig kakao = new ProviderConfig();
	private ProviderConfig naver = new ProviderConfig();
	
	@Getter
	@Setter
	public static class ProviderConfig{
		private String authorizationUri;
		private String tokenUri;
		private String userInfoUri;
		private String userNameAttribute;
	}

}
