package org.ssafy.sid.users.model.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.ssafy.sid.exception.UserNotFoundException;
import org.ssafy.sid.images.service.ImageSaveServiceImpl;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.lastprofiles.service.LastProfileServiceImpl;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.profiles.model.ProfilesRepository;
import org.ssafy.sid.users.dto.LoginUserDTO;
import org.ssafy.sid.users.jwt.JwtUtil;
import org.ssafy.sid.users.model.Users;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLoginServiceImpl implements UserLoginService {

	private final JwtUtil jwtUtil;
	private final UsersServiceImpl usersServiceImpl;
	private final ProfilesRepository profilesRepository;
	private final LastProfileServiceImpl lastProfileServiceImpl;
	private final ImageSaveServiceImpl imageSaveServiceImpl;

	public ResponseEntity<Map<String, Object>> userLogin(LoginUserDTO loginUser, Users user, HttpServletResponse response, Boolean isRemember) throws IOException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		Boolean profileIsEmpty = lastProfileServiceImpl.getLastProfile(user);
		String accessToken = jwtUtil.createAccessToken(loginUser.getEmail());
		String refreshToken = jwtUtil.createRefreshToken(loginUser.getEmail(), isRemember);

		// 발급 받은 refresh token을 DB에 저장
		try {
			usersServiceImpl.saveRefreshToken(loginUser.getUser_id(), refreshToken);
		} catch (IllegalArgumentException e) {
			errorResultMap.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResultMap);
		} catch (NoSuchElementException e) {
			errorResultMap.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		} catch (UserNotFoundException e) {
			errorResultMap.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResultMap);
		}

		Cookie accessTokenCookie = getTokenCookie(accessToken, "accessToken");
		Cookie refreshTokenCookie = getTokenCookie(refreshToken, "refreshToken");

		// 쿠키에 토큰 추가
		response.addCookie(accessTokenCookie);
		response.addCookie(refreshTokenCookie);
		resultMap.put("email", loginUser.getEmail()); // 로그인된 사용자 정보 추가
		resultMap.put("message", "login");
		if (!profileIsEmpty) {
			Profiles lastProfile = loginUser.getLastProfile();
			Map<String, Object> lastProfileMap = new HashMap<>();
			lastProfileMap.put("nickname", lastProfile.getNickname());
			lastProfileMap.put("profileImage", imageSaveServiceImpl.checkImage(lastProfile.getProfileImage()));
			resultMap.put("lastProfile", lastProfileMap);
		}

		List<Map<String, Object>> profileList = new ArrayList<>();
		List<Profiles> profilesList = profilesRepository.findByUser(user);
		if (!profilesList.isEmpty()) {
			for (Profiles profile : profilesList) {
				Map<String, Object> profileMap = new HashMap<>();
				profileMap.put("nickname", profile.getNickname());
				profileMap.put("profileImage", imageSaveServiceImpl.checkImage(profile.getProfileImage()));

				profileList.add(profileMap);
			}
		}

		resultMap.put("profileList", profileList);

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	private static Cookie getTokenCookie(String token, String tokenValue) {
		Cookie tokenCookie = new Cookie(tokenValue, token);
		tokenCookie.setHttpOnly(true);
		tokenCookie.setSecure(false);
		tokenCookie.setPath("/");
		tokenCookie.setMaxAge(60 * 60 * 24 * 365); // 1년 유효
		return tokenCookie;
	}
}
