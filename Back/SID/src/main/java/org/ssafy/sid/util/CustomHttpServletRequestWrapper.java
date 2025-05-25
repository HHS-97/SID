package org.ssafy.sid.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
	private final String newAccessToken;

	public CustomHttpServletRequestWrapper(HttpServletRequest request, String newAccessToken) {
		super(request);
		this.newAccessToken = newAccessToken;
	}

	@Override
	public Cookie[] getCookies() {
		Cookie[] originalCookies = super.getCookies();
		if (originalCookies != null) {
			boolean tokenFound = false;
			for (Cookie cookie : originalCookies) {
				if ("accessToken".equals(cookie.getName())) {
					cookie.setValue(newAccessToken);
					tokenFound = true;
				}
			}
			if (!tokenFound) {
				// 기존 쿠키 배열에 새 쿠키를 추가하는 방법 (배열 재구성)
				Cookie newCookie = new Cookie("accessToken", newAccessToken);
				newCookie.setPath("/");
				Cookie[] newCookies = new Cookie[originalCookies.length + 1];
				System.arraycopy(originalCookies, 0, newCookies, 0, originalCookies.length);
				newCookies[originalCookies.length] = newCookie;
				return newCookies;
			}
		}
		return originalCookies;
	}
}
