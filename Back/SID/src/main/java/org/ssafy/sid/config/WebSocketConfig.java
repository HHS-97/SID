package org.ssafy.sid.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.ssafy.sid.interceptor.JwtHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final ChannelInterceptor channelInterceptor;
	private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

	public WebSocketConfig(ChannelInterceptor channelInterceptor, JwtHandshakeInterceptor jwtHandshakeInterceptor) {
		this.channelInterceptor = channelInterceptor;
		this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/api/chat-ws")
				.setAllowedOriginPatterns(
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
				).addInterceptors(jwtHandshakeInterceptor).withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/app");
//		registry.enableSimpleBroker("/topic");
		// Use this for enabling a Full featured broker like RabbitMQ
		registry.enableStompBrokerRelay("/topic")
				.setRelayHost("43.202.53.108")
				.setRelayPort(61613)
				.setClientLogin("guest")
				.setClientPasscode("guest");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(channelInterceptor);
	}
}
