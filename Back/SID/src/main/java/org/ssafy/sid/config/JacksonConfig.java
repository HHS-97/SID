package org.ssafy.sid.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		// Java 8 날짜 타입 직렬화를 위해 등록
		mapper.registerModule(new JavaTimeModule());
		// Hibernate 프록시 객체를 위한 모듈 등록
//		mapper.registerModule(new Hibernate5Module());

		return mapper;
	}
}
