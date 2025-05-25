package org.ssafy.sid.follow.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ssafy.sid.follow.dto.FollowsDTO;
import org.ssafy.sid.follow.dto.FollowsRequestDTO;
import org.ssafy.sid.follow.model.Follows;
import org.ssafy.sid.follow.model.FollowsRepository;
import org.ssafy.sid.follow.service.FollowsServiceImpl;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.lastprofiles.model.LastProfilesRepository;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.profiles.model.ProfilesRepository;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.jwt.JwtUtil;
import org.ssafy.sid.users.jwt.service.JwtServiceImpl;
import org.ssafy.sid.users.model.Users;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/follows")
public class FollowsController {

	private final JwtUtil jwtUtil;
	private final UsersRepository usersRepository;
	private final ProfilesRepository profilesRepository;
	private final LastProfilesRepository lastProfilesRepository;
	private final FollowsServiceImpl followsServiceImpl;
	private final FollowsRepository followsRepository;
	private final JwtServiceImpl jwtServiceImpl;

	@PostMapping
	public ResponseEntity<?> addFollow(@RequestBody FollowsRequestDTO followsRequestDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();

		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = "";
		if (getEmail.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		} else {
			return getEmail;
		}
		Optional<Users> user = usersRepository.findByEmail(email);

		if (user.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());
		if (lastProfiles.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		if (!profilesRepository.existsById(lastProfiles.get(0).getProfile().getId())) {
			errorResultMap.put("error", "존재하지 않는 프로필입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<Profiles> following = profilesRepository.findByNickname(followsRequestDTO.getFollowNickname());
		if (following.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 프로필입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		if (followsRequestDTO.getIsFollowed() || followsRepository.existsByFollowerAndFollowing(lastProfiles.get(0).getProfile(), following.get())) {
			errorResultMap.put("error", "이미 팔로우한 프로필입니다.");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResultMap);
		}

		FollowsDTO followsDTO = FollowsDTO.builder()
				.follower(lastProfiles.get(0).getProfile())
				.following(following.get())
				.build();

		followsServiceImpl.addFollow(followsDTO);

		resultMap.put("isFollow", "True");
		resultMap.put("followedNickName", following.get().getNickname());
		resultMap.put("message", "successful");

		return ResponseEntity.ok().body(resultMap);
	}

	@DeleteMapping
	public ResponseEntity<?> deleteFollow(@RequestBody FollowsRequestDTO followsRequestDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();

		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = "";
		if (getEmail.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		} else {
			return getEmail;
		}
		Optional<Users> user = usersRepository.findByEmail(email);

		if (user.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());
		if (lastProfiles.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		if (!profilesRepository.existsById(lastProfiles.get(0).getProfile().getId())) {
			errorResultMap.put("error", "존재하지 않는 프로필입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<Profiles> following = profilesRepository.findByNickname(followsRequestDTO.getFollowNickname());
		if (following.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 프로필입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<Follows> follow = followsRepository.findByFollowerAndFollowing(lastProfiles.get(0).getProfile(), following.get());
		if (follow.isEmpty()) {
			errorResultMap.put("error", "팔로우 한적이 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		followsServiceImpl.deleteFollow(follow.get());

		resultMap.put("isFollow", "False");
		resultMap.put("followedNickName", following.get().getNickname());
		resultMap.put("message", "successful");

		return ResponseEntity.ok().body(resultMap);
	}

	@GetMapping("/following")
	public ResponseEntity<?> getFollowing(@RequestParam String nickname, HttpServletRequest request, HttpServletResponse response) throws IOException {
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
		Optional<Users> user = usersRepository.findByEmail(email);

		if (user.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());
		if (lastProfiles.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		if (!lastProfilesRepository.existsByProfile(lastProfiles.get(0).getProfile())) {
			errorResultMap.put("error", "존재하지 않는 프로필입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<Profiles> profile = profilesRepository.findByNickname(nickname);
		if (profile.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 프로필입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		return ResponseEntity.status(HttpStatus.OK).body(followsServiceImpl.getFollowings(profile.get(), lastProfiles.get(0).getProfile()));
	}

	@GetMapping("/follower")
	public ResponseEntity<?> getFollower(@RequestParam String nickname, HttpServletRequest request, HttpServletResponse response) throws IOException {
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
		Optional<Users> user = usersRepository.findByEmail(email);

		if (user.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());
		if (lastProfiles.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		if (!lastProfilesRepository.existsByProfile(lastProfiles.get(0).getProfile())) {
			errorResultMap.put("error", "존재하지 않는 프로필입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<Profiles> profile = profilesRepository.findByNickname(nickname);
		if (profile.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 프로필입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		return ResponseEntity.status(HttpStatus.OK).body(followsServiceImpl.getFollowers(profile.get(), lastProfiles.get(0).getProfile()));
	}
}
