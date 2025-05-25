package org.ssafy.sid.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.ssafy.sid.chat.dto.ChatMessageDTO;

@Configuration
@EnableRedisRepositories
public class RedisConfig {
	@Value("${spring.data.redis.host}")
	private String host;

	@Value("${spring.data.redis.port}")
	private int port;

	@Value("${spring.data.redis.password}")
	private String password;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
		redisConfiguration.setHostName(host);
		redisConfiguration.setPort(port);
		redisConfiguration.setPassword(password);
		return new LettuceConnectionFactory(redisConfiguration);
	}

	// primary RedisTemplate 빈 (Key: String, Value: Object)
	@Bean
	@Primary
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());

		// 키와 해시 키는 String 직렬화
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());

		// 값과 해시 값은 JSON 직렬화 (Profiles 등 Serializable하지 않은 객체도 처리 가능)
		Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
		redisTemplate.setValueSerializer(jsonSerializer);
		redisTemplate.setHashValueSerializer(jsonSerializer);

		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	// ChatMessageDTO 전용 RedisTemplate
	@Bean
	public RedisTemplate<String, ChatMessageDTO> redisTemplateMessage(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, ChatMessageDTO> redisTemplateMessage = new RedisTemplate<>();
		redisTemplateMessage.setConnectionFactory(connectionFactory);
		redisTemplateMessage.setKeySerializer(new StringRedisSerializer());
		redisTemplateMessage.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatMessageDTO.class));
		return redisTemplateMessage;
	}

	// Object 전용 RedisTemplate
	@Bean
	public RedisTemplate<String, Object> redisTemplateObject(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
		return template;
	}
}
