package org.ssafy.sid;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.TimeZone;

//(exclude={SecurityAutoConfiguration.class})
@SpringBootApplication
public class SidApplication {

	public static void main(String[] args) {
		// .env 파일 로드
		Dotenv dotenv = Dotenv.load();

		// 환경 변수 설정
		System.setProperty("MAIL_ADDRESS", dotenv.get("MAIL_ADDRESS"));
		System.setProperty("APP_PASSWORD", dotenv.get("APP_PASSWORD"));
		System.setProperty("IMAGE_SRC", dotenv.get("IMAGE_SRC"));
		System.setProperty("DATABASE_URL", dotenv.get("DATABASE_URL"));
		System.setProperty("DATABASE_PASS", dotenv.get("DATABASE_PASS"));
		System.setProperty("JWT_KEY", dotenv.get("JWT_KEY"));
		System.setProperty("KAKAO_ID", dotenv.get("KAKAO_ID"));
		System.setProperty("KAKAO_SECRECT", dotenv.get("KAKAO_SECRECT"));
		System.setProperty("NAVER_ID", dotenv.get("NAVER_ID"));
		System.setProperty("NAVER_SECRET", dotenv.get("NAVER_SECRET"));
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(SidApplication.class, args);
	}

}

