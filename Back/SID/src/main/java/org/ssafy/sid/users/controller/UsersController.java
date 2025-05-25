package org.ssafy.sid.users.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.ssafy.sid.exception.UserNotFoundException;
import org.ssafy.sid.lastprofiles.dto.LastProfileSaveDTO;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.lastprofiles.model.LastProfilesRepository;
import org.ssafy.sid.lastprofiles.service.LastProfileServiceImpl;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.profiles.model.ProfilesRepository;
import org.ssafy.sid.response.ErrorResponse;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.dto.*;
import org.ssafy.sid.users.jwt.JwtUtil;
import org.ssafy.sid.users.jwt.service.JwtServiceImpl;
import org.ssafy.sid.users.model.Users;
import org.ssafy.sid.users.model.service.EmailServiceImpl;
import org.ssafy.sid.users.model.service.UserDetailServiceImpl;
import org.ssafy.sid.users.model.service.UserLoginServiceImpl;
import org.ssafy.sid.users.model.service.UsersServiceImpl;
import org.ssafy.sid.util.CustomResponseBuilder;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UsersController {

	private static final Logger log = LoggerFactory.getLogger(UsersController.class);
	private final UsersServiceImpl usersService;
	private final JwtUtil jwtUtil;
	private final EmailServiceImpl emailService;
	private final UserDetailServiceImpl userDetailServiceImpl;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final UsersRepository usersRepository;
	private final UsersServiceImpl usersServiceImpl;
	private final LastProfilesRepository lastProfilesRepository;
	private final LastProfileServiceImpl lastProfileServiceImpl;
	private final ProfilesRepository profilesRepository;
	private final View error;
	private final JwtServiceImpl jwtServiceImpl;
	private final CustomResponseBuilder customResponseBuilder;
	private final UserLoginServiceImpl userLoginServiceImpl;
	private final EmailServiceImpl emailServiceImpl;

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@Validated @RequestBody UsersSaveDTO usersSaveDTO) {
		return usersServiceImpl.create(usersSaveDTO);
	}

	@GetMapping("/emailvalid")
	public ResponseEntity<Map<String, Object>> emailValid(@RequestParam("email") String email) {
		boolean isValid = usersService.checkEmailDuplicate(email);
		Map<String, Object> response = new HashMap<>();

		if (isValid) {
			String code = emailService.joinEmail(email);
			response.put("isValid", true);
			return ResponseEntity.ok(response);
		} else {
			response.put("isValid", false);
			return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
		}
	}

	@GetMapping("/verify")
	public ResponseEntity<Map<String, Object>> verify(@RequestParam("email") String email, @RequestParam("code") String code) {
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, Object> errorResultMap = new HashMap<>();
		boolean result = emailServiceImpl.checkAuthNum(email, code);
		if (!result) {
			errorResultMap.put("error", "no");
			errorResultMap.put("isVerify", false);
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResultMap);
		}

		resultMap.put("message", "ok");
		resultMap.put("isVerify", true);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO, HttpServletResponse response) throws IOException {
		Optional<Users> user = usersRepository.findByEmail(loginDTO.getEmail());

		if (user.isPresent()) {
			// 비밀번호 검증
			if (!bCryptPasswordEncoder.matches(loginDTO.getPassword(), user.get().getPassword())) {
				log.error("비밀번호가 일치하지 않습니다.");
				return customResponseBuilder.passwordNotMatches();
			}
		} else {
			log.error("이메일이 일치하지 않습니다.");
			return customResponseBuilder.emailNotMatches();
		}

		LoginUserDTO loginUser = usersService.loginUser(loginDTO, user.get());
		return userLoginServiceImpl.userLogin(loginUser, user.get(), response, loginDTO.isRememberMe());
	}

	@PatchMapping("/detail")
	public ResponseEntity<?> update(@Validated @RequestBody UserUpdateDTO userUpdateDTO, HttpServletRequest request, HttpServletResponse response) {
		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = "";
		if (getEmail.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		} else {
			return getEmail;
		}

		return usersServiceImpl.updateUser(email, userUpdateDTO);
	}

	@GetMapping("/detail")
	public ResponseEntity<?> userDetail(@Validated HttpServletRequest request, HttpServletResponse response) {
		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = "";
		if (getEmail.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		} else {
			return getEmail;
		}

		try {
			// 추출한 이메일을 사용하여 사용자 상세 정보 로드
			UserDetailDTO userDetails = userDetailServiceImpl.loadUserByEmail(email);

			// 사용자 상세 정보를 HTTP 200 OK 상태와 함께 반환
			return ResponseEntity.status(HttpStatus.OK).body(userDetails);
		} catch (UserNotFoundException e) {
			// 사용자 미존재 시 처리
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 또는 적절한 에러 객체 반환
		} catch (Exception e) {
			// 기타 예외 처리
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("사용자 정보 조회에 예상치못한 에러가 발생했습니다." + e.getMessage()); // 또는 적절한 에러 객체 반환
		}
	}

	@DeleteMapping("/login")
	public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
		String email = null;
		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmailLogout(request);
		if (getEmail.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		} else {
			return getEmail;
		}
		

		try {
			// 로그아웃 로직: 예를 들어, refresh token 삭제 또는 블랙리스트 추가
			usersService.deleteRefreshToken(email);

			// 쿠키 삭제
			Cookie accessTokenCookie = new Cookie("accessToken", null);
			accessTokenCookie.setHttpOnly(true);
			accessTokenCookie.setSecure(true);
			accessTokenCookie.setPath("/");
			accessTokenCookie.setMaxAge(0); // 쿠키 삭제
//			accessTokenCookie.setSameSite("Strict"); // CSRF 방지를 위한 설정

			Cookie refreshTokenCookie = new Cookie("refreshToken", null);
			refreshTokenCookie.setHttpOnly(true);
			refreshTokenCookie.setSecure(true);
			refreshTokenCookie.setPath("/");
			refreshTokenCookie.setMaxAge(0); // 쿠키 삭제
//			refreshTokenCookie.setSameSite("Strict"); // CSRF 방지를 위한 설정

			response.addCookie(accessTokenCookie);
			response.addCookie(refreshTokenCookie);

			Map<String, String> resultMap = Map.of("message", "logout");
			return ResponseEntity.status(HttpStatus.OK).body(resultMap);
		} catch (UserNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@PatchMapping("/detail/password")
	public ResponseEntity<?> updatePassword(@Validated @RequestBody PasswordDTO passwordDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = null;
		if (getEmail.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		} else {
			return getEmail;
		}

		Users user = usersRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

		// 저장된 비밀번호
		String password = user.getPassword();
		// 받아온 현재 비밀번호
		String currentPassword = passwordDTO.getCurrentPassword();
		// 새 비밀번호
		String newPassword1 = passwordDTO.getNewPassword1();
		// 새 비밀번호 확인
		String newPassword2 = passwordDTO.getNewPassword2();

		if (!bCryptPasswordEncoder.matches(currentPassword, password)) {
			errorResultMap.put("error", "현재 비밀번호가 다릅니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
		}
		if (!newPassword1.equals(newPassword2)) {
			errorResultMap.put("error", "새 비밀번호가 같지 않습니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
		}

		usersServiceImpl.updatePassword(email, newPassword1);

		resultMap.put("message", "successful");

		return ResponseEntity.status(HttpStatus.OK)
				.body(resultMap);
	}
}
