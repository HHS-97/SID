package org.ssafy.sid.interceptor;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.ssafy.sid.users.jwt.JwtUtil;

@Service
public class AuthChannelInterceptor implements ChannelInterceptor {
	private final JwtUtil jwtUtil;

	public AuthChannelInterceptor(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor.getCommand() == StompCommand.CONNECT) {
			// HandshakeInterceptor에서 저장한 토큰 값 가져오기
			String accessToken = (String) accessor.getSessionAttributes().get("accessToken");
			String refreshToken = (String) accessor.getSessionAttributes().get("refreshToken");
			if (refreshToken != null && accessToken == null && jwtUtil.isTokenExpired(accessToken)) {
				try {
					String email = jwtUtil.extractEmail(refreshToken);
					String newAccessToken = jwtUtil.createAccessToken(email);
					// 새로운 accessToken으로 세션 속성 업데이트
					accessor.getSessionAttributes().put("accessToken", newAccessToken);
					accessToken = newAccessToken;
				} catch (ExpiredJwtException e) {
					String errorMessage = "Access token expired: " + e.getMessage();
					throw new MessagingException(errorMessage, e);
				}
			} else if (refreshToken == null && accessToken == null) {
				throw new AuthenticationCredentialsNotFoundException("토큰이 없습니다.");
			}

		}
		return message;
	}
}
