package org.ssafy.sid.users.jwt;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.model.Users;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtUtil {
	private final RefreshTokensRepository refreshTokensRepository;
	private final UsersRepository usersRepository;
	@Value("${jwt.secretKey}")
	private String secretKey; // jwt 토큰 객체 키를 저장할 시크릿 키

	@Value("${jwt.access-token.expiretime}")
	private long accessTokenExpireTime;

	@Value("${jwt.refresh-token.expiretime}")
	private long refreshTokenExpireTime;

	@PostConstruct
	private void validateSecretKey() {
		if (secretKey == null || secretKey.isEmpty()) {
			throw new IllegalStateException("JWT SecretKey가 설정되지 않았습니다.");
		}
	}

	public String createAccessToken(String email) {
		return createJwt(email, "accessToken", accessTokenExpireTime);
	}

	// accesstoken에 비해서 유효기간을 길게 설정
	public String createRefreshToken(String email, Boolean remember) {
		if (remember) {
			return createJwt(email, "refreshToken", 86400000L * 365);
		} else {
			return createJwt(email, "refreshToken", refreshTokenExpireTime);
		}
	}

	private byte[] generateKey() {
			// charset 설정 안하면 사용자 플랫폼의 기본 인코딩 설정으로 인코딩 됨
			return secretKey.getBytes(StandardCharsets.UTF_8);
	}

	public String createJwt(String email, String subject, long expiredTime) {
		// Payload 설정 : 생성일 (IssuedAt), 유효기간 (Expiration),
		// 토큰 제목 (Subject), 데이터(Claim) 등 정보 세팅.
		Claims claims = Jwts.claims()
				.setSubject(subject) // 토큰 제목 설정 ex) access-token, refresh-token
				.setIssuedAt(new Date())  // 생성일 설정
				// 만료일 설정 (유효기간)
				.setExpiration(new Date(System.currentTimeMillis() + expiredTime));

		// 저장할 data의 key, value
		claims.put("email", email);

		String jwt = Jwts.builder()
				// Header 설정 : 토큰의 타입, 해쉬 알고리즘 정보 세팅
				.setHeaderParam("typ", "JWT").setClaims(claims)
				// Signature 설정 : secret key를 활용한 암호화.
				.signWith(SignatureAlgorithm.HS256, this.generateKey())
				.compact(); // 직렬화

		return jwt;
	}

	public boolean checkToken(String token) {
		try{
			/*
			* Json Web Signature : 서버에서 인증을 근거로 인증 정보를 서버의 private key 서명 한것을 토큰화 한것
			* setSigningKey : JWS 서명 검증을 위한 secret key 세팅
			* parseClaimsJws : 파싱하여 원본 jws 만들기
			*/
			Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(this.generateKey()).build().parseClaimsJws(token);
			// Claims는 Map 구현체 형태
			log.debug("claims : {}", claims);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		}
	}

	public String getExpirationDateAsString(String token) {
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(this.generateKey()).build().parseClaimsJws(token).getBody();
			Date expirationDate = claims.getExpiration();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
			return formatter.format(expirationDate.toInstant());
		} catch (Exception e) {
			return null;
		}
	}

	// true면 만료됨
	public Boolean isTokenExpired(String token) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		if (getExpirationDateAsString(token) == null) {
			return true;
		}

		LocalDateTime expirationDate = LocalDateTime.parse(getExpirationDateAsString(token), formatter);

		return expirationDate.isBefore(LocalDateTime.now());
	}

	public String extractEmail(String token) {
		Claims claims;
		try {
			claims = Jwts.parserBuilder()
					.setSigningKey(generateKey())
					.build()
					.parseClaimsJws(token)
					.getBody();
		} catch (ExpiredJwtException eje) {
			// 토큰이 만료되었더라도 claims를 가져옵니다.
			claims = eje.getClaims();
		}

		return claims.get("email", String.class);
	}

	public boolean isValidToken(String token, String email) {
		try {
			final String tokenEmail = extractEmail(token);
			return tokenEmail.equals(email) && !isTokenExpired(token);
		} catch (ExpiredJwtException e) {
			log.warn("토큰이 만료되었습니다: {}", e.getMessage());
			return false;
		} catch (Exception e) {
			log.error("토큰 검증 중 오류 발생: {}", e.getMessage());
			return false;
		}
	}

	public boolean isValidRefreshToken(String token, String email) {
		if (isTokenExpired(token)) {
			return false;
		} else {
			return true;
		}
	}
}


