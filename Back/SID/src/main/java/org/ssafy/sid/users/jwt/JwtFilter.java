package org.ssafy.sid.users.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.ssafy.sid.util.CookieUtils;
import org.ssafy.sid.util.CustomHttpServletRequestWrapper;
import org.ssafy.sid.exception.InvalidTokenException;
import org.ssafy.sid.exception.TokenNotFoundException;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.model.service.UserDetailServiceImpl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * JWT 인증을 처리하는 필터 클래스
 * 모든 요청에 대해 JWT 토큰을 검증하고, 유효한 경우 인증 정보를 SecurityContext에 설정
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

	private final UserDetailServiceImpl userDetailServiceImpl;
	private final JwtUtil jwtUtil;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();
		return path.startsWith("/chat-ws");
	}

	// JWT 인증을 제외할 URL 패턴 목록
	private static final List<String> EXCLUDED_PATHS = Arrays.asList(
			"/api/",
			"/api/user/signup",
			"/api/user/login",
			"/api/user/emailvalid",
			"/api/swagger-ui/**",
			"/api/v3/**", "/api/home",
			"/api/posts/briefly",
			"/api/cookie",
			"/api/login/oauth2/**",
			"/api/uploads/**",
			"/api/posts/more",
			"/api/posts/one",
			"/api/posts/search",
			"/chat-ws/**",
			"/api/user/verify",
			"/api/social/**"
	);

	// JWT 인증을 제외할 URL 패턴 목록
	private final AntPathMatcher antPathMatcher = new AntPathMatcher();
	private final UsersRepository usersRepository;

	/**
	 * 실제 필터링 로직을 구현하는 메서드
	 * 요청이 들어올 때마다 실행되어 JWT 토큰을 검증하고 인증 정보를 설정
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String path = request.getRequestURI();

		// Permit-All 경로는 JWT 인증을 건너뜀
		// 제외 경로인지 확인
		boolean isExcluded = EXCLUDED_PATHS.stream()
				.anyMatch(pattern -> antPathMatcher.match(pattern, path));
		if (isExcluded) {
			log.info("Path is excluded from JWT authentication: {}", path);
			filterChain.doFilter(request, response);
			return;
		}

		String email = null;
		String accessToken = null;
		// 기본적으로 원본 request를 사용
		HttpServletRequest requestToUse = request;

		// 쿠키에서 토큰 추출
		Optional<Cookie> accessTokenCookie = CookieUtils.getCookie(request, "accessToken");
		Optional<Cookie> refreshTokenCookie = CookieUtils.getCookie(request, "refreshToken");

		// accessToken과 refreshToken 둘 다 없으면 커스텀 에러 응답 처리
		if (refreshTokenCookie.isEmpty() && accessTokenCookie.isEmpty()) {
			response.setContentType("application/json");
			// 401(Unauthorized) 또는 403(Forbidden)을 사용할 수 있는데, 인증되지 않았음을 명시하려면 보통 401을 사용합니다.
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("{\"error\": \"JWT 토큰이 제공되지 않았습니다.\"}");
			return;
		}

		if (accessTokenCookie.isPresent()) {
			accessToken = accessTokenCookie.get().getValue();
			email = jwtUtil.extractEmail(accessToken);
			if (jwtUtil.isTokenExpired(accessToken)) {
				// refreshToken을 통해 새로운 accessToken 발급 및 SecurityContext 갱신
				try {
					String newAccessToken = handleExpiredAccessToken(request, response, email);
					// 발급된 토큰으로 request를 래핑하여 후속 처리 시 새 토큰을 사용하도록 함
					requestToUse = new CustomHttpServletRequestWrapper(request, newAccessToken);
					// accessToken 변수도 새 토큰으로 업데이트
					accessToken = newAccessToken;
				} catch (InvalidTokenException | TokenNotFoundException e) {
					response.setContentType("application/json");
					// 401(Unauthorized) 또는 403(Forbidden)을 사용할 수 있는데, 인증되지 않았음을 명시하려면 보통 401을 사용합니다.
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
					return;
				}

			}
		} else {
			// accessToken 쿠키가 없을 경우 refreshToken으로부터 email을 추출하고 새 accessToken 발급 시도
			String refreshToken = refreshTokenCookie.get().getValue();
			log.info("accessToken 쿠키가 없으므로 refreshToken으로부터 이메일 추출");
			email = jwtUtil.extractEmail(refreshToken);
			try {
				String newAccessToken = handleExpiredAccessToken(request, response, email);
				// 발급된 토큰으로 request를 래핑하여 후속 처리 시 새 토큰을 사용하도록 함
				requestToUse = new CustomHttpServletRequestWrapper(request, newAccessToken);
				// accessToken 변수도 새 토큰으로 업데이트
				accessToken = newAccessToken;
			} catch (InvalidTokenException | TokenNotFoundException e) {
				response.setContentType("application/json");
				// 401(Unauthorized) 또는 403(Forbidden)을 사용할 수 있는데, 인증되지 않았음을 명시하려면 보통 401을 사용합니다.
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
				return;
			}
		}

		// email이 존재하고, 아직 SecurityContext에 인증 정보가 등록되지 않았다면
		if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			// isValidToken 내부에서 이메일 비교와 만료 여부를 함께 체크하므로, 추가 만료 검사는 생략
			if (!jwtUtil.isValidToken(accessToken, email)) {
				try {
					CookieUtils.deleteCookie(request, response, "accessToken");
					String newAccessToken = handleExpiredAccessToken(request, response, email);
					requestToUse = new CustomHttpServletRequestWrapper(request, newAccessToken);
					accessToken = newAccessToken;
				} catch (InvalidTokenException | TokenNotFoundException e) {
					response.setContentType("application/json");
					// 401(Unauthorized) 또는 403(Forbidden)을 사용할 수 있는데, 인증되지 않았음을 명시하려면 보통 401을 사용합니다.
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
					return;
				}
			}
			// 최종적으로 인증 정보를 SecurityContext에 등록
			setAuthentication(email, requestToUse);
		}
		filterChain.doFilter(requestToUse, response);
	}

	private String handleExpiredAccessToken(HttpServletRequest request, HttpServletResponse response, String email) {
		Optional<Cookie> refreshTokenCookie = CookieUtils.getCookie(request, "refreshToken");

		if (refreshTokenCookie.isPresent()) {
			String refreshToken = refreshTokenCookie.get().getValue();

			if (jwtUtil.isValidRefreshToken(refreshToken, email)) {
				String newAccessToken = jwtUtil.createAccessToken(email);

				// 새로운 accessToken 쿠키 설정
				Cookie newAccessTokenCookie = new Cookie("accessToken", newAccessToken);
				newAccessTokenCookie.setPath("/");
				newAccessTokenCookie.setHttpOnly(true);
				newAccessTokenCookie.setMaxAge(60 * 60 * 24 * 365); // 1년
				response.addCookie(newAccessTokenCookie);
				setAuthentication(email, request);
				return newAccessToken;
			} else {
				log.warn("유효하지 않은 refreshToken입니다.");
				CookieUtils.deleteCookie(request, response, "refreshToken");
				throw new InvalidTokenException("유효하지 않은 refreshToken입니다.");
			}
		} else {
			log.warn("refreshToken이 존재하지 않습니다.");
			throw new TokenNotFoundException("refreshToken이 존재하지 않습니다.");
		}
	}


	private void setAuthentication(String email, HttpServletRequest request) {
		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(email, null, List.of());
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
