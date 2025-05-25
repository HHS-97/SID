package org.ssafy.sid.users.model.service;

public interface EmailService {
	/* 이메일 전송 */
	public void mailSend(String setFrom, String toMail, String title, String content);
	/* 이메일 작성 */
	public String joinEmail(String email);
	/* 인증번호 확인 */
	public Boolean checkAuthNum(String email, String authNum);
}
