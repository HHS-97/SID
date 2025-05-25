package org.ssafy.sid.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.security.auth.message.config.AuthConfigFactory.RegistrationContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration")
public class OAuth2RegistartionProperties {

	private RegistartionConfig kakao;
	private RegistartionConfig naver;
	
	@Getter
	@Setter
	public static class RegistartionConfig {
		private String clientId;
		private String clientSecret;
		private String redirectUri;
		private String authorizationGrantType;
		private String clientAuthenticationMethod;
		private String clientName;
		private List<String> scope;
		
		
		public void setScope(List<String> scope) {
            // 각 스코프에 kakao_account 접두사를 붙입니다.
			
			this.scope = scope.stream()
	                .map(s -> {
	                    if (clientName != null && clientName.equalsIgnoreCase("Kakao")) {
	                        return "kakao_account." + s;
	                    } else if (clientName != null && clientName.equalsIgnoreCase("Naver")) {
	                        return "naver_account." + s;
	                    }
	                    return s; // 기본적으로 원래 스코프 반환
	                })
	                .toList();
        }
	}
	
}
