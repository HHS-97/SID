package org.ssafy.sid.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.ssafy.sid.util.constants.CustomHttpStatus;
import org.ssafy.sid.withdraws.model.Withdraws;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomResponseBuilder {
	private final Map<String, Object> errorResultMap = new HashMap<String, Object>();

	public ResponseEntity<Map<String, Object>> emailDuplicate() {
		errorResultMap.put("error", "중복된 이메일입니다.");
		return ResponseEntity.status(CustomHttpStatus.EmailDuplicate).body(errorResultMap);
	}

	public ResponseEntity<Map<String, Object>> withdrawUser(Withdraws withdraw) {
		errorResultMap.put("error", "회원탈퇴 신청한 계정입니다.");
		return ResponseEntity.status(CustomHttpStatus.WithdrawUser).body(errorResultMap);
	}

	public ResponseEntity<Map<String, Object>> passwordNotMatches() {
		errorResultMap.put("error", "이메일 또는 비밀번호를 확인하세요.");
		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResultMap);
	}

	public ResponseEntity<Map<String, Object>> emailNotMatches() {
		errorResultMap.put("error", "이메일 또는 비밀번호를 확인하세요.");
		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResultMap);
	}

	public ResponseEntity<Map<String, Object>> emailOrPasswordNotMatches() {
		errorResultMap.put("error", "이메일 또는 패스워드를 확인해주세요.");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResultMap);
	}

	public ResponseEntity<Map<String, Object>> userNotFound() {
		errorResultMap.put("error", "사용자를 찾을 수 없습니다.");
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
	}
}
