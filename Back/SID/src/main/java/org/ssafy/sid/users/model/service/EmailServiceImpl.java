package org.ssafy.sid.users.model.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
	private final JavaMailSender javaMailSender;
	private final @Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate;
	//		private RedisConfig redisConfig;
	private int authNumber;

	/* 이메일 인증에 필요한 정보 */
	@Value("${spring.mail.username}")
	private String serviceName;

	/* 랜덤 인증번호 생성 */
	private int makeRandomNum() {
		Random r = new Random();
		return r.nextInt(900000) + 100000; // 100000 ~ 999999
	}

	/* 이메일 전송 */
	@Override
	@Transactional
	public void mailSend(String setFrom, String toMail, String title, String content) {
		MimeMessage message = javaMailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message,true,"utf-8");
			helper.setFrom(setFrom); // service name
			helper.setTo(toMail); // customer email
			helper.setSubject(title); // email title
			helper.setText(content,true); // content, html: true
			javaMailSender.send(message);
		} catch (MessagingException e) {
			e.printStackTrace(); // 에러 출력
		}
	}

	/* 이메일 작성 */
	@Override
	@Transactional
	public String joinEmail(String email) {
		int authNumber = makeRandomNum();
		String customerMail = email;
		String title = "회원 가입을 위한 이메일입니다!";
		String content =
				"이메일을 인증하기 위한 절차입니다." +
						"<br><br>" +
						"인증 번호는 " + authNumber + "입니다." +
						"<br>" +
						"회원 가입 폼에 해당 번호를 입력해주세요.";
		mailSend(serviceName, customerMail, title, content);
		// redis에 3분 동안 이메일과 인증 코드 저장
		ValueOperations<String, String> valOperations = redisTemplate.opsForValue();
		valOperations.set(email, Integer.toString(authNumber), 180, TimeUnit.SECONDS);
		return Integer.toString(authNumber);
	}

	/* 인증번호 확인 */
	@Override
	@Transactional
	public Boolean checkAuthNum(String email, String authNum) {
		ValueOperations<String, String> valOperations = redisTemplate.opsForValue();
		Object codeObj = valOperations.get(email);
		if (codeObj == null) {
			return false;
		}

		String code = codeObj.toString();
		return code.equals(authNum);
	}
}
