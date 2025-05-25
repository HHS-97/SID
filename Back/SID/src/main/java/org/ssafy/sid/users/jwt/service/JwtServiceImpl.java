package org.ssafy.sid.users.jwt.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.ssafy.sid.response.ErrorResponse;
import org.ssafy.sid.users.jwt.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {

	private final JwtUtil jwtUtil;

	@Override
	@Transactional
	public ResponseEntity<Map<String, Object>> getEmail(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<>();
		String accessToken = null;
		String email = "";
		Cookie[] cookies = request.getCookies();
		// accessToken 쿠키 추출
		if (cookies != null) {
			accessToken = findCookieValue(cookies, "accessToken");
		} else {
			// 쿠키가 비었을 때 반환하는 에러 메세지
			return nullCookie();
		}

		// accessToken이 없는 경우 처리
		if (accessToken == null) {
			// accessToken이 없으면 refreshToken으로부터 이메일 추출 및 새 토큰 발급 시도
			String refreshToken = findCookieValue(cookies, "refreshToken");

			if (refreshToken != null) {
				if (jwtUtil.isTokenExpired(refreshToken)) {
					// refreshToken이 만료된 경우: 에러 응답
					result.put("error", "유효하지 않은 토큰입니다.");
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
				} else {
					email = jwtUtil.extractEmail(refreshToken);
				}
			} else {
				result.put("Unauthorized", "유효하지 않은 토큰입니다.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
			}
		} else {
			try {
				email = jwtUtil.extractEmail(accessToken);
			} catch (Exception e) {
				String refreshToken = null;
				for (Cookie cookie : cookies) {
					if ("refreshToken".equals(cookie.getName())) {
						refreshToken = cookie.getValue();
						break;
					}
				}

				if (refreshToken != null) {
					if (jwtUtil.isTokenExpired(refreshToken)) {
						result.put("Unauthorized", "유효하지 않은 토큰입니다.");
						return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
					} else {
						email = jwtUtil.extractEmail(refreshToken);
						accessToken = jwtUtil.createAccessToken(email);
					}
				} else {
					log.error("토큰에서 이메일 추출 실패: {}", e.getMessage());
					result.put("Unauthorized", "유효하지 않은 토큰입니다.");
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
				}
			}
		}

		// 성공적으로 이메일을 추출한 경우 결과에 추가
		result.put("email", email);
		result.put("accessToken", accessToken);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@Override
	@Transactional
	public ResponseEntity<Map<String, Object>> getEmailLogout(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<>();
		String accessToken = null;
		String email = "";
		Cookie[] cookies = request.getCookies();
		// accessToken 쿠키 추출
		if (cookies != null) {
			accessToken = findCookieValue(cookies, "accessToken");
		} else {
			// 쿠키가 비었을 때 반환하는 에러 메세지
			return nullCookie();
		}

		// accessToken이 없는 경우 처리
		if (accessToken == null) {
			// accessToken이 없으면 refreshToken으로부터 이메일 추출 및 새 토큰 발급 시도
			String refreshToken = findCookieValue(cookies, "refreshToken");

			if (refreshToken != null) {
					email = jwtUtil.extractEmail(refreshToken);
			} else {
				result.put("Unauthorized", "유효하지 않은 토큰입니다.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
			}
		} else {
			try {
				email = jwtUtil.extractEmail(accessToken);
			} catch (Exception e) {
				String refreshToken = null;
				for (Cookie cookie : cookies) {
					if ("refreshToken".equals(cookie.getName())) {
						refreshToken = cookie.getValue();
						break;
					}
				}

				if (refreshToken != null) {
					if (jwtUtil.isTokenExpired(refreshToken)) {
						result.put("Unauthorized", "유효하지 않은 토큰입니다.");
						return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
					} else {
						email = jwtUtil.extractEmail(refreshToken);
						accessToken = jwtUtil.createAccessToken(email);
					}
				} else {
					log.error("토큰에서 이메일 추출 실패: {}", e.getMessage());
					result.put("Unauthorized", "유효하지 않은 토큰입니다.");
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
				}
			}
		}

		// 성공적으로 이메일을 추출한 경우 결과에 추가
		result.put("email", email);
		result.put("accessToken", accessToken);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	private static ResponseEntity<Map<String, Object>> nullCookie() {
		Map<String, Object> errorResult = new HashMap<>();
		errorResult.put("email", "");
		errorResult.put("accessToken", "");
		errorResult.put("error", "쿠키 없음");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
	}


	public String findCookieValue(Cookie[] cookies, String name) {
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
