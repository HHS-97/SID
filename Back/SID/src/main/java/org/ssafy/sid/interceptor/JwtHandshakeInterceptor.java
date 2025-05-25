package org.ssafy.sid.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
								   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		log.debug("Before handshake intercepted: {}", request.getURI());
		log.debug("Before handshake intercepted: {}", request.getURI());
		if (request instanceof ServletServerHttpRequest servletRequest) {
			HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
			Cookie[] cookies = httpServletRequest.getCookies();
			if (cookies != null) {
				Arrays.stream(cookies).forEach(cookie ->
						log.debug("Cookie: {}={}", cookie.getName(), cookie.getValue())
				);
				// 토큰 추출 로직
			} else {
				log.debug("No cookies found in request");
			}
		}

		if (request instanceof ServletServerHttpRequest servletRequest) {
			log.debug("실행");
			HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
			Cookie[] cookies = httpServletRequest.getCookies();
			if (cookies != null) {
				// accessToken 저장
				Optional<Cookie> accessTokenCookie = Arrays.stream(cookies)
						.filter(cookie -> "accessToken".equals(cookie.getName()))
						.findFirst();
				accessTokenCookie.ifPresent(cookie -> {
					attributes.put("accessToken", cookie.getValue());
				});

				// refreshToken 저장
				Optional<Cookie> refreshTokenCookie = Arrays.stream(cookies)
						.filter(cookie -> "refreshToken".equals(cookie.getName()))
						.findFirst();
				refreshTokenCookie.ifPresent(cookie -> {
					attributes.put("refreshToken", cookie.getValue());
				});
			}
		}
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
							   WebSocketHandler wsHandler, Exception exception) {
	}
}
