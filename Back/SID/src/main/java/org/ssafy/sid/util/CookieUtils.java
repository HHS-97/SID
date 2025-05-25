package org.ssafy.sid.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public class CookieUtils {

	// 쿠키를 가져오는 메서드
	public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
		// request에 담긴 쿠키를 할당
		Cookie[] cookies = request.getCookies();

		// 쿠키가 null이 아니고 쿠키의 길이가 0보다 크면
		if (cookies != null && cookies.length > 0) {
			// cookies안의 쿠키들을
			for (Cookie cookie : cookies) {
				// name과 이름이 같으면
				if (cookie.getName().equals(name)) {
					// Optional.of로 해당 쿠키를 감싸 반환합니다.
					return Optional.of(cookie);
				}
			}
		}
		// 만약 쿠키가 존재하지 않거나, 찾고자하는 이름의 쿠키가 없다면 Optional.empty()를 반환
		return Optional.empty();
	}

	// maxAge는 쿠키가 브라우저에서 유지되는 기간
	// 0이상이면 해당시간동안 쿠키가 유지되다가 만료
	// 0으로 설정되면 쿠키가 즉시 만료
	// -1로 설정하면 브라우저가 종료될 때 쿠키가 사라짐(세션 쿠키로 동작)
	public static void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
		setCookie(response, name, value, false, false, maxAge);
	}

	public static void setCookie(HttpServletResponse response, String name, String value, boolean secure, boolean httpOnly, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setSecure(secure);
		cookie.setHttpOnly(httpOnly);
		cookie.setMaxAge(maxAge);
		response.addCookie(cookie);
	}

	public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					cookie.setValue("");
					cookie.setPath("/");
					cookie.setMaxAge(0);
					response.addCookie(cookie);
				}
			}
		}
	}
}
