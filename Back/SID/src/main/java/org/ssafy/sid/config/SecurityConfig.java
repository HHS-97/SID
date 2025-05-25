package org.ssafy.sid.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.ssafy.sid.users.jwt.JwtFilter;
import org.ssafy.sid.users.jwt.LoginFilter;

import java.util.Arrays;
import java.util.Collections;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity  // 스프링 시큐리티 필터가 스프링 필터체인에 등록 된다.
public class SecurityConfig {

	private final AuthenticationConfiguration authenticationConfiguration;

	private final JwtFilter jwtFilter;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	private CorsConfigurationSource configurationSource() {
			CorsConfiguration config = new CorsConfiguration();
			config.setAllowedHeaders(Collections.singletonList("*"));
			config.setAllowedMethods(Collections.singletonList("*"));
			config.setAllowedOriginPatterns(Arrays.asList(
					"http://localhost:3000",
					"http://43.202.53.108:8084",
					"http://localhost:5173",
					"http://43.202.53.108:8082",
					"https://localhost:3000",
					"https://43.202.53.108:8084",
					"https://localhost:5173",
					"https://43.202.53.108:8082",
					"https://i12c110.p.ssafy.io", 
        			"http://i12c110.p.ssafy.io"
			));	
//			 config.setAllowedOriginPatterns(Collections.singletonList("*")); // 모든 출처 허용
			config.setAllowCredentials(true);

			UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
			source.registerCorsConfiguration("/**", config);
			return source;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// 메서드 참조 방식 (AbstractHttpConfigurer::disable):
				//코드가 다소 짧고, 가독성이 좋다고 느낄 수 있다
				//단순히 disable만 하고 싶은 경우 유용하다
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.cors(corsConfig -> corsConfig.configurationSource(configurationSource()))
				.authorizeHttpRequests(authorizeRequests ->
						authorizeRequests
						.requestMatchers("/api/home",
								"/api/user/signup",
								"/api/user/login",
					            "/api/user/emailvalid",
								"/api/swagger-ui/**",
								"/api/v3/**",
								"/api/",
					            "/api/posts/briefly",
								"/api/oauth2/**",
								"/api/login/oauth2/**",
								"/api/cookie",
								"/api/uploads/**",
								"/api/posts/more",
								"/chat-ws/**",
								"/api/posts/one",
								"/api/posts/search",
								"/api/user/verify",
								"/api/social/**"
						) // ✅ 이 부분 확인
					        .permitAll()
								// 모든 요청에 대해 인증을 요구한다.
								.anyRequest().authenticated()
				)
				.sessionManagement(sessionManagement ->
						sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.formLogin(AbstractHttpConfigurer::disable)
//				.oauth2Login(Customizer.withDefaults()) // oAuth2 로그인 활성화
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
		;
		return http.build();
	}


	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
