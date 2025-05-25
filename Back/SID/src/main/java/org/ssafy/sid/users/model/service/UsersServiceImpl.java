package org.ssafy.sid.users.model.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ssafy.sid.exception.InvalidPasswordException;
import org.ssafy.sid.exception.UserNotFoundException;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.lastprofiles.model.LastProfilesRepository;
import org.ssafy.sid.users.dto.*;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.jwt.JwtUtil;
import org.ssafy.sid.users.jwt.RefreshTokens;
import org.ssafy.sid.users.jwt.RefreshTokensRepository;
import org.ssafy.sid.users.model.Users;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersServiceImpl implements UsersService {

	private final UsersRepository usersRepository;

	private final RefreshTokensRepository refreshTokensRepository;

	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	private final JwtUtil jwtUtil;
	private final LastProfilesRepository lastProfilesRepository;

	@Override
	@Transactional
	public ResponseEntity<Map<String, Object>> create(UsersSaveDTO usersSaveDTO) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();

		if (usersRepository.existsByEmail(usersSaveDTO.getEmail())) {
			log.error("이미 존재하는 이메일입니다.");
			errorResultMap.put("error", "이미 존재하는 이메일입니다.");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResultMap);
		}

		// DTO에서 비밀번호 추출
		String rawPassword = usersSaveDTO.getPassword();
		String rawPasswordConfirm = usersSaveDTO.getPasswordConfirm();

		if (!rawPassword.equals(rawPasswordConfirm)) {
			log.error("비밀번호 확인이 틀렸습니다.");
			errorResultMap.put("error", "비밀번호 확인이 틀렸습니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
		}

		// 비밀번호 암호화
		String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);
		// save
		Users user = Users.builder()
				.email(usersSaveDTO.getEmail())
				.password(encodedPassword)
				.name(usersSaveDTO.getName())
				.gender(usersSaveDTO.getGender().charAt(0))
				.birthDate(usersSaveDTO.getBirthDate())
				.phone(usersSaveDTO.getPhone())
				.build();

		usersRepository.save(user);
		resultMap.put("email", user.getEmail());
		resultMap.put("message", "create");

		return ResponseEntity.status(HttpStatus.CREATED).body(resultMap);
	}

	@Override
	@Transactional
	public Boolean checkEmailDuplicate(String email) {
		// 중복검사를 통과했으면 true를 보내기 위해서 !를 앞에 넣어둠
		return !usersRepository.existsByEmail(email);
	}

	@Override
	@Transactional
	public LoginUserDTO loginUser(LoginDTO loginDTO, Users user) {
		LoginUserDTO loginUserDTO = new LoginUserDTO();

		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user);
		if (!lastProfiles.isEmpty()) {
			loginUserDTO.setLastProfile(lastProfiles.get(0).getProfile());
		}

		loginUserDTO.setEmail(user.getEmail());
		loginUserDTO.setUser_id(user.getId());

		return loginUserDTO;
	}

	@Override
	@Transactional
	public void saveRefreshToken(long user_id, String refreshToken) {
		// 사용자 조회
		Users user = usersRepository.findById(user_id).orElseThrow(() -> new UserNotFoundException("refreshToken 저장 중 사용자를 찾을 수 없습니다."));
		// 리프레시 토큰 만료 시간 추출
		String expireTime = jwtUtil.getExpirationDateAsString(refreshToken);
		if (expireTime == null) {
			throw new NoSuchElementException("리프레시 토큰이 없습니다.");
		}

		// 기존에 동일한 토큰이 있는지 확인
		if (refreshTokensRepository.existsByRefreshToken(refreshToken)) {
			throw new IllegalArgumentException("이미 존재하는 리프레시 토큰입니다.");
		}

		List<RefreshTokens> refreshTokensList = refreshTokensRepository.findAllByUser(user);
		if (!refreshTokensList.isEmpty()) {
			refreshTokensRepository.deleteAll(refreshTokensList);
		}

		RefreshTokens refreshTokens = RefreshTokens.builder()
				.user(user)
				.refreshToken(refreshToken)
				.refreshExpire(expireTime)
				.build();

		refreshTokensRepository.save(refreshTokens);
	}

	@Override
	@Transactional
	// 사용자 조회
	public UserDetailDTO userDetail(String email) {
		Optional<Users> user = usersRepository.findByEmail(email);
		UserDetailDTO userDetailDTO = new UserDetailDTO();

		if (user.isPresent()) {
			return userDetailDTO.toUserDetailDTO(user.get());
		} else {
			throw new UserNotFoundException("사용자를 찾을 수 없습니다. 이메일: " + email);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<Map<String, Object>> updateUser(String email, UserUpdateDTO userUpdateDTO) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		Optional<Users> user = usersRepository.findByEmail(email);

		if (user.isPresent()) {
			Users userEntity = user.get();
			userEntity.update(userUpdateDTO);
		} else {
			log.error("사용자를 찾을 수 없습니다.");
			errorResultMap.put("error", "사용자를 찾을 수 없습니다.");
			errorResultMap.put("email", email);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		resultMap.put("message", "update");
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@Override
	@Transactional
	public void deleteRefreshToken(String email) {
		Users user = usersRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("등록되지 않은 회원입니다."));
		refreshTokensRepository.deleteByUser(user);
	}

	@Override
	@Transactional
	public void updatePassword(String email, String password) {
		// 비밀번호 암호화
		String encodedPassword = new BCryptPasswordEncoder().encode(password);

		Users user = usersRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("등록되지 않은 회원입니다."));

		UserUpdateDTO updateDTO = UserUpdateDTO.builder()
				.password(encodedPassword)
				.build();

		user.update(updateDTO);
	}
}
