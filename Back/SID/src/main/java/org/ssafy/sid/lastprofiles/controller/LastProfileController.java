package org.ssafy.sid.lastprofiles.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ssafy.sid.lastprofiles.dto.ChangeLastProfileRequestDTO;
import org.ssafy.sid.lastprofiles.dto.LastProfileUpdateDTO;
import org.ssafy.sid.lastprofiles.service.LastProfileServiceImpl;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.profiles.model.ProfilesRepository;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.jwt.JwtUtil;
import org.ssafy.sid.users.jwt.service.JwtServiceImpl;
import org.ssafy.sid.users.model.Users;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/lastprofiles")
public class LastProfileController {

	private final LastProfileServiceImpl lastProfileService;
	private final JwtUtil jwtUtil;
	private final UsersRepository usersRepository;
	private final ProfilesRepository profilesRepository;
	private final JwtServiceImpl jwtServiceImpl;

	@PatchMapping
	public ResponseEntity<?> changeProfile(@Validated @RequestBody ChangeLastProfileRequestDTO changeLastProfileRequestDTO, HttpServletRequest request, HttpServletResponse response) {
		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = null;
		if (getEmail.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		} else {
			return getEmail;
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();

		Optional<Users> user = usersRepository.findByEmail(email);
		if (user.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		Optional<Profiles> profile = profilesRepository.findByNickname(changeLastProfileRequestDTO.getNickname());
		if (profile.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 프로필입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		LastProfileUpdateDTO lastProfileUpdateDTO = LastProfileUpdateDTO.builder().profile(profile.get()).build();

		String lastProfileNickname = lastProfileService.updateLastProfiles(lastProfileUpdateDTO, user.get());

		resultMap.put("nickname", lastProfileNickname);
		resultMap.put("message", "successful");

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}
}
