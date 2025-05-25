package org.ssafy.sid.users.dto;

import lombok.*;
import org.ssafy.sid.users.jwt.RefreshTokens;
import org.ssafy.sid.users.model.Users;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RefreshTokenDTO {
	private Users user;
	private String refreshToken;
	private String refreshExpire;

	public static RefreshTokenDTO toDto(RefreshTokens refreshTokens) {
		if (refreshTokens == null) {
			return null;
		}

		return RefreshTokenDTO.builder()
				.refreshToken(refreshTokens.getRefreshToken())
				.refreshExpire(refreshTokens.getRefreshExpire().toString())
				.build();
	}

	public static RefreshTokens toEntity(RefreshTokenDTO refreshTokenDto, Users user) {
		if (refreshTokenDto == null) {
			return null;
		}

		return RefreshTokens.builder()
				.user(user)
				.refreshToken(refreshTokenDto.getRefreshToken())
				.refreshExpire(String.valueOf(LocalDateTime.parse(refreshTokenDto.getRefreshExpire())))
				.build();
	}
}
