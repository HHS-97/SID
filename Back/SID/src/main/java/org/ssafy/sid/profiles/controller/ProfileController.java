package org.ssafy.sid.profiles.controller;

import java.io.IOException;
import java.util.*;

import jakarta.validation.constraints.Null;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ssafy.sid.follow.model.FollowsRepository;
import org.ssafy.sid.images.service.ImageSaveServiceImpl;
import org.ssafy.sid.posts.dto.PostsGetDTO;
import org.ssafy.sid.posts.service.PostsServiceImpl;
import org.ssafy.sid.profiles.service.ProfileService;
import org.ssafy.sid.users.jwt.service.JwtServiceImpl;
import org.ssafy.sid.util.CustomResponseBuilder;
import org.ssafy.sid.exception.ProfileMaxException;
import org.ssafy.sid.exception.UserNotFoundException;
import org.ssafy.sid.lastprofiles.dto.LastProfileSaveDTO;
import org.ssafy.sid.lastprofiles.dto.LastProfileUpdateDTO;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.lastprofiles.model.LastProfilesRepository;
import org.ssafy.sid.lastprofiles.service.LastProfileServiceImpl;
import org.ssafy.sid.profiles.dto.ProfileDeleteDTO;
import org.ssafy.sid.profiles.dto.ProfileDetailDTO;
import org.ssafy.sid.profiles.dto.ProfileListDTO;
import org.ssafy.sid.profiles.dto.ProfileSaveDTO;
import org.ssafy.sid.profiles.dto.ProfileUpdateDTO;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.profiles.model.ProfilesRepository;
import org.ssafy.sid.profiles.service.ProfileServiceImpl;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.controller.UsersController;
import org.ssafy.sid.users.jwt.JwtUtil;
import org.ssafy.sid.users.model.Users;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

	private static final Logger log = LoggerFactory.getLogger(UsersController.class);
	private final ProfileServiceImpl profileService;
	private final JwtUtil jwtUtil;
	private final UsersRepository usersRepository;
	private final ProfilesRepository profilesRepository;
	private final LastProfileServiceImpl lastProfileServiceImpl;
	private final ProfileServiceImpl profileServiceImpl;
	private final LastProfilesRepository lastProfilesRepository;
	@Autowired
	private final CustomResponseBuilder responseBuilder;
	@Autowired
	private FollowsRepository followsRepository;
	@Autowired
	private JwtServiceImpl jwtServiceImpl;
	@Autowired
	private PostsServiceImpl postsServiceImpl;
	@Autowired
	private ImageSaveServiceImpl imageSaveServiceImpl;

	@PostMapping
	public ResponseEntity<?> createProfile(@ModelAttribute ProfileSaveDTO profileSaveDTO, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		Users user = null;
		Profiles profile = null;
		if (profileSaveDTO.getNickname() == null || profileSaveDTO.getNickname().isBlank()) {
			log.error("닉네임이 비어있습니다.");
			errorResultMap.put("error", "닉네임이 비어있습니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
		}


		if (profilesRepository.existsByNickname(profileSaveDTO.getNickname())) {
			log.error("이미 사용중인 닉네임입니다.");
			errorResultMap.put("error", "이미 사용중인 닉네임입니다.");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResultMap);
		}
		
		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = "";
		if (getEmail.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		} else {
			return getEmail;
		}

		try {
			user = usersRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("등록되지 않은 유저입니다."));
		} catch (UserNotFoundException e) {
			errorResultMap.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		profileSaveDTO.setUser(user);

		if (profilesRepository.findByUser(profileSaveDTO.getUser()).size() >= 5) {
			log.error("더 이상 프로필을 생성할 수 없습니다.");
			errorResultMap.put("error", "더 이상 프로필을 생성할 수 없습니다.");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResultMap);
		}

		try {
			profile = profileService.createProfile(profileSaveDTO);
		} catch (NullPointerException e) {
			errorResultMap.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user);

		if (lastProfile.isEmpty()) {
			LastProfileSaveDTO lastProfileSaveDTO = LastProfileSaveDTO.builder()
					.user(user)
					.profile(profile)
					.build();
			lastProfileServiceImpl.createLastProfiles(lastProfileSaveDTO);
		} else if (lastProfile.size() > 1) {
			LastProfiles remainLastProfiles = lastProfile.get(0);
			lastProfile.remove(0);
			lastProfilesRepository.deleteAll(lastProfile);
		} else {
			LastProfileUpdateDTO lastProfileUpdateDTO = LastProfileUpdateDTO.builder().profile(profile).build();
			lastProfileServiceImpl.updateLastProfiles(lastProfileUpdateDTO, user);
		}

		ProfileListDTO profileListDTO;
		try {
			profileListDTO = profileService.profileList(email);
		} catch (UserNotFoundException e) {
			log.error(e.getMessage());
			errorResultMap.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		resultMap.put("profiles", profileListDTO.getProfiles());
		resultMap.put("lastProfile", profileListDTO.getLastProfile());

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@GetMapping("/nicknamevalid")
	public ResponseEntity<?> nicknameValid(@RequestParam("nickname") String nickname) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		
		if (nickname == null || nickname.isBlank()) {
			log.error("닉네임이 비어있습니다.");
			errorResultMap.put("error", "닉네임이 비어있습니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
		}
		if (profilesRepository.existsByNickname(nickname)) {
			log.error("닉네임이 중복됩니다.");
			errorResultMap.put("error", "닉네임이 중복됩니다.");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResultMap);
		}

		resultMap.put("message", "ok");
		resultMap.put("nickname", nickname);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@GetMapping
	public ResponseEntity<?> getProfile(@RequestParam("nickname") String nickname, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		if (nickname == null || nickname.isBlank()) {
			log.error("닉네임이 비어있습니다.");
			errorResultMap.put("error", "닉네임이 비어있습니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
		}

		Optional<Profiles> profiles = profilesRepository.findByNickname(nickname);
		if (profiles.isEmpty()) {
			log.error("프로필이 존재하지 않습니다.");
			errorResultMap.put("error", "프로필이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		} else {
			ProfileDetailDTO profileDetailDTO = profileService.profileDetail(profiles.get());
			resultMap.put("nickname", profileDetailDTO.getNickname());
			resultMap.put("description", profileDetailDTO.getDescription());
			resultMap.put("profileImage", profileDetailDTO.getProfileImageUrl());
			resultMap.put("interestCategories", profileDetailDTO.getInterestCategories());
			resultMap.put("followerCount", profileDetailDTO.getFollowerCount());
			resultMap.put("followingCount", profileDetailDTO.getFollowingCount());

			Users user = null;
			LastProfiles lastProfile = null;
			Profiles following = profiles.get();
			ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
			String email = null;
			if (getEmail.getStatusCode() == HttpStatus.OK) {
				Map<String, Object> body = getEmail.getBody();
				email = (String) body.get("email");
			} else {
				return getEmail;
			}

			Optional<Users> users = usersRepository.findByEmail(email);
			if (users.isPresent()) {
				user = users.get();
				List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user);
				lastProfile = lastProfiles.get(0);
			}


			if (lastProfile != null) {
				Profiles follower = lastProfile.getProfile();
				if (followsRepository.existsByFollowerAndFollowing(follower, following)) {
					resultMap.put("isFollowed", "YES");
				} else if (follower.equals(following)) {
					resultMap.put("isFollowed", "ME");
				} else {
					resultMap.put("isFollowed", "NO");
				}
			} else {
				resultMap.put("isFollowed", "NO");
			}

			resultMap.put("postsCount", profileDetailDTO.getPostsCount());
			return ResponseEntity.status(HttpStatus.OK).body(resultMap);
		}
	}

	@PatchMapping
	public ResponseEntity<?> updateProfile(@ModelAttribute ProfileUpdateDTO profileUpdateDTO, HttpServletRequest request, HttpServletResponse response) throws IOException {
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
			log.error("존재하지 않는 유저입니다.");
			errorResultMap.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());
		if (lastProfiles.isEmpty()) {
			log.error("최근 프로필 기록이 존재하지 않습니다.");
			errorResultMap.put("error", "최근 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		LastProfiles lastProfile = lastProfiles.get(0);
		Profiles profile = lastProfile.getProfile();

		if (!profilesRepository.existsByNickname(profile.getNickname())) {
			log.error("프로필이 존재하지 않습니다.");
			errorResultMap.put("error", "프로필이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		profileServiceImpl.updateProfile(profileUpdateDTO, profile);

		List<LastProfiles> newlastProfiles = lastProfilesRepository.findByUser(user.get());
		if (lastProfiles.isEmpty()) {
			log.error("최근 프로필 기록이 존재하지 않습니다.");
			errorResultMap.put("error", "최근 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		LastProfiles newLastProfile = newlastProfiles.get(0);
		Profiles newProfile = newLastProfile.getProfile();

		if (!profilesRepository.existsByNickname(newProfile.getNickname())) {
			log.error("프로필이 존재하지 않습니다.");
			errorResultMap.put("error", "프로필이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Map<String, Object> lastProfileMap = new HashMap<>();
		lastProfileMap.put("nickname", newProfile.getNickname());
		lastProfileMap.put("profileImage", imageSaveServiceImpl.checkImage(newProfile.getProfileImage()));
		resultMap.put("lastProfile", lastProfileMap);

		List<Map<String, Object>> profileList = new ArrayList<>();
		List<Profiles> profilesList = profilesRepository.findByUser(user.get());
		if (!profilesList.isEmpty()) {
			for (Profiles useProfile : profilesList) {
				Map<String, Object> profileMap = new HashMap<>();
				profileMap.put("nickname", useProfile.getNickname());
				profileMap.put("profileImage", imageSaveServiceImpl.checkImage(useProfile.getProfileImage()));

				profileList.add(profileMap);
			}
		}
		resultMap.put("profileList", profileList);
		
		resultMap.put("message", "update");

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@DeleteMapping
	public ResponseEntity<?> deleteProfile(@RequestBody ProfileDeleteDTO profileDeleteDTO, HttpServletRequest request, HttpServletResponse response) {
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
		String nickname = profileDeleteDTO.getNickname();
		Optional<Users> user = usersRepository.findByEmail(email);

		if (user.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		if (profilesRepository.findByUser(user.get()).size() <= 1) {
			errorResultMap.put("error", "프로필을 더 이상 삭제할 수 없습니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
		}

		if (!profilesRepository.existsByNickname(nickname)) {
			errorResultMap.put("error", "존재하지 않는 프로필입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		if (!profilesRepository.existsByNicknameAndUser(nickname, user.get())) {
			errorResultMap.put("error", "다른 사람의 프로필입니다.");
			errorResultMap.put("user", user.get().getEmail());
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResultMap);
		}
		profileServiceImpl.deleteProfile(nickname);

		List<Profiles> profiles = profilesRepository.findByUser(user.get());
		LastProfileSaveDTO lastProfileSaveDTO = LastProfileSaveDTO.builder()
				.profile(profiles.get(0))
				.user(user.get())
				.build();
		lastProfileServiceImpl.createLastProfiles(lastProfileSaveDTO);

		ProfileListDTO profileListDTO;
		try {
			profileListDTO = profileService.profileList(email);
		} catch (UserNotFoundException | IOException e) {
			log.error(e.getMessage());
			errorResultMap.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		resultMap.put("lastProfile", profileListDTO.getLastProfile());
		resultMap.put("profiles", profileListDTO.getProfiles());
		resultMap.put("message", "delete");
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@GetMapping("/list")
	public ResponseEntity<?> getProfilesList(HttpServletRequest request, HttpServletResponse response) {
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

		ProfileListDTO profileListDTO;
		try {
			profileListDTO = profileService.profileList(email);
		} catch (UserNotFoundException e) {
			log.error(e.getMessage());
			errorResultMap.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		resultMap.put("profiles", profileListDTO.getProfiles());
		resultMap.put("lastProfile", profileListDTO.getLastProfile());

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@GetMapping("/posts")
	public ResponseEntity<?> getProfilePostsList(@RequestParam(value = "page", defaultValue = "0") int page,
												 @RequestParam(value = "nickname") String nickname,
												 HttpServletRequest request,
												 HttpServletResponse response) {
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
			log.error("최근 프로필 기록이 존재하지 않습니다.");
			errorResultMap.put("error", "최근 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		LastProfiles lastProfile = lastProfiles.get(0);
		Profiles myProfile = lastProfile.getProfile();
		Optional<Profiles> thisProfile = profilesRepository.findByNickname(nickname);

		if (!profilesRepository.existsByNickname(myProfile.getNickname()) || thisProfile.isEmpty()) {
			log.error("프로필이 존재하지 않습니다.");
			errorResultMap.put("error", "프로필이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Page<PostsGetDTO> postPage = postsServiceImpl.getProfilePosts(page, myProfile, thisProfile.get());
		List<PostsGetDTO> posts = postPage.getContent();
		resultMap.put("posts", posts);

		return ResponseEntity.status(HttpStatus.OK).body(posts);
	}
}
