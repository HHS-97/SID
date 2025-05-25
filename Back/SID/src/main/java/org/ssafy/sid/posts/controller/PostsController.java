package org.ssafy.sid.posts.controller;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.ssafy.sid.curating.service.CuratingServiceImpl;
import org.ssafy.sid.fcm.Dto.NotificationDto;
import org.ssafy.sid.fcm.model.FcmToken;
import org.ssafy.sid.fcm.service.FcmServiceImpl;
import org.ssafy.sid.follow.model.Follows;
import org.ssafy.sid.follow.model.FollowsRepository;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.lastprofiles.model.LastProfilesRepository;
import org.ssafy.sid.posts.dto.*;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.posts.model.PostsRepository;
import org.ssafy.sid.posts.service.PostsServiceImpl;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.profiles.model.ProfilesRepository;
import org.ssafy.sid.profiles.service.ProfileServiceImpl;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.jwt.JwtUtil;
import org.ssafy.sid.users.jwt.service.JwtServiceImpl;
import org.ssafy.sid.users.model.Users;
import org.ssafy.sid.fcm.repository.FcmRepository;
import org.ssafy.sid.fcm.repository.NotificationRepository;
import org.ssafy.sid.fcm.model.Notification;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostsController {

	private final UsersRepository usersRepository;
	private final JwtUtil jwtUtil;
	private final LastProfilesRepository lastProfilesRepository;
	private final PostsServiceImpl postsServiceImpl;
	private final ProfileServiceImpl profileServiceImpl;
	private final PostsRepository postsRepository;
	private final JwtServiceImpl jwtServiceImpl;
	private final FcmServiceImpl fcmServiceImpl;
	private final FcmRepository fcmRepository;
	private final FollowsRepository followrepository;
	private final ProfilesRepository profileRepository;
	private final WebClient webClient;
	private final CuratingServiceImpl curatingServiceImpl;
	private final NotificationRepository notificationRepository;

	@PostMapping
	public ResponseEntity<?> createPosts(@Valid @ModelAttribute PostsSaveDTO postsSaveDTO, HttpServletRequest request, HttpServletResponse response) throws IOException {
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

		List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user.get());
		if (lastProfile.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		
		Profiles profile = lastProfile.get(0).getProfile();
		
		// 로그인 한 사용자를 팔로우한 사람들의 리스트
		List<Follows> followList = followrepository.findByFollowing(profile);
		
		// 팔로잉 리스트 목록들의 사람들의 아이디 찾아냄
//		List<Long> followedUserIds = new ArrayList<>();
//			for (Follows follows : followList) {
//			followedUserIds.add(follows.getFollower().getUser().getId());
//			System.out.println("사용자 아이디 입니다" + follows.getFollower().getUser().getId());	
//		}		
		
		// 팔로잉 리스트 목록들의 사람들의 fcm 토큰을 가져옴
//		List<String> Fcmtokens = new ArrayList<>();
//		for(Long id : followedUserIds) {
//			System.out.println("여기는 아이디입니다" + id);
//			List<String> fcmTokens = fcmServiceImpl.getFcmTokens(id);
//			Fcmtokens.addAll(fcmTokens);
//		}
		
		Posts post = postsServiceImpl.createPost(postsSaveDTO, lastProfile.get(0).getProfile());
		
		String type = "posts";
		
		for(Follows follow : followList) {
			Long followId = follow.getFollower().getUser().getId();
//			System.out.println("팔로우 아이디의 개수는 " + followList.size());
			List<String> fcmTokens = fcmServiceImpl.getFcmTokens(followId);
//			System.out.println("여기는 fcm의 개수입니다" + fcmTokens.size());
			
			if(fcmTokens != null && fcmTokens.isEmpty() == false) {
				Notification notification = Notification.builder()
		    			.title(post.getProfile().getNickname() + "님이 글을 작성하셨습니다.")
		    			.body(post.getContent())
		    			.isRead(false)
		    			.type(type) // 여기까진 가능
		    			.referenceId(post.getId()) // 이게 무슨 글에 된건가?
		    			.receiver(follow.getFollower()) // 받는사람
		    			.sender(profile) // 보내느 사람
		    			.image(profile.getProfileImage()) // 보내는 사람 프로필 이미지
		    			.build();
				notificationRepository.save(notification);
				for(String token : fcmTokens) {
//					System.out.println("토큰에 대한 정보 입니다" + token);
					String title = post.getProfile().getNickname() + "님이 글을 작성하셨습니다.";
					
					boolean flag = fcmServiceImpl.sendNotificationWithData(token, title, post.getContent() ,type, post.getId(), "Posts");
					//String token, String title, String body, String type, Long referenceId, Long userId, String image
					
					
				}
//				Notification notification = Notification.builder()
//		    			.title(post.getProfile().getNickname() + "님이 글을 작성하셨습니다.")
//		    			.body(post.getContent())
//		    			.isRead(false)
//		    			.type(type) // 여기까진 가능
//		    			.referenceId(post.getId()) // 이게 무슨 글에 된건가?
//		    			.receiver(follow.getFollower()) // 받는사람
//		    			.sender(profile) // 보내느 사람
//		    			.image(profile.getProfileImage()) // 보내는 사람 프로필 이미지
//		    			.build();
//				notificationRepository.save(notification);
			}
		}
		
//
//		List<NotificationDto> notificationDtos = new ArrayList<>();	
//		String type = "posts";
//		if(Fcmtokens != null && !Fcmtokens.isEmpty()) {
//			for(String token : Fcmtokens) {
//				System.out.println("FCM Token : " + token);
//				boolean flag = fcmServiceImpl.sendNotificationWithData(token ,post.getProfile().getNickname(),post.getContent(), type , post.getId(), user.get().getId(), post.getProfile().getProfileImage());
//				if(flag == true) {
//					System.out.println("좋아 메시지가 보내졌어");
//					NotificationDto notificationDto = NotificationDto.builder()
//							.title(post.getProfile().getNickname() + "님이 글을 작성하셨습니다.")
//							.body(post.getContent())
//							.isRead(false)
//							.type(type)
//							.
//							.referenceId(null) 
//							.build();
//					
//					notificationDtos.add(notificationDto);
//					System.out.println("알림 Dto 입니다 ==== "  + notificationDto.getTitle());
//				}
//				else {
//					System.out.println("메세지가 보내지지 않았어");
//				}
//			}
//		}
//		else {
//			System.out.println("토큰은 없어 없어 없어 없어");
//		}
//		//테스트 용으로 보내봄
//		if (Fcmtokens != null && !Fcmtokens.isEmpty()) {
//		    for (String token : Fcmtokens) {
//		        boolean test = fcmServiceImpl.sendNotificationWithData(token, post.getProfile().getNickname(), post.getContent(), type, post.getId(), user.get().getId(), post.getProfile().getProfileImage());
//		        System.out.println("이건 팔로우 친구들 모임이야 " + token);
//		        if (test) {
//		            System.out.println("보내짐");
//		        } else {
//		            System.out.println("보내지지 않음");
//		        }
//		    }
//		} else {
//		    System.out.println("토큰이 없습니다.");
//		}

		resultMap.put("postId", post.getId());
		resultMap.put("nickname", post.getProfile().getNickname());
		resultMap.put("message", "create");
//		resultMap.put("notifications", notificationDtos);	

		return ResponseEntity.status(HttpStatus.CREATED).body(resultMap);
	}

	@PatchMapping
	public ResponseEntity<?> updatePosts(@Valid @ModelAttribute PostsUpdateDTO postsUpdateDTO, HttpServletRequest request, HttpServletResponse response) throws IOException {
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

		List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user.get());
		if (lastProfile.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<Posts> post = postsRepository.findById(postsUpdateDTO.getPostId());

		if (post.isEmpty()) {
			errorResultMap.put("error", "해당 게시글이 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Profiles profile = lastProfile.get(0).getProfile();

		if (profile != post.get().getProfile()) {
			errorResultMap.put("error", "게시글 작성자가 아닙니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
		}

		postsServiceImpl.updatePost(postsUpdateDTO, post.get());

		resultMap.put("postId", postsUpdateDTO.getPostId());
		resultMap.put("message", "update");
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@DeleteMapping
	public ResponseEntity<?> deletePosts(@Valid @RequestBody PostsDeleteDTO postsDeleteDTO, HttpServletRequest request, HttpServletResponse response) throws IOException {
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

		List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user.get());
		if (lastProfile.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<Posts> post = postsRepository.findById(postsDeleteDTO.getPostId());
		if (post.isEmpty()) {
			errorResultMap.put("error", "해당 게시글이 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		if (!lastProfile.get(0).getProfile().getNickname().equals(post.get().getProfile().getNickname())) {
			errorResultMap.put("error", "게시글 작성자가 아닙니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
		}

		postsServiceImpl.deletePost(post.get());
		resultMap.put("message", "delete");

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@GetMapping("/briefly")
	public ResponseEntity<?> getPosts(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "type", defaultValue = "1") int type, @RequestParam(value = "postIds", required = false) List<Long> postIds, HttpServletRequest request, HttpServletResponse response) throws IOException {
		// 큐레이팅 완성되면 닉네임으로 가중치 찾아서 적용해야됨
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();

		if (postIds == null || postIds.isEmpty()) {
			postIds = new ArrayList<>();
			postIds.add(-1L);
		}

		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = null;
		Profiles profile = null;
		if (getEmail.getStatusCode() == HttpStatus.OK && getEmail.getBody().get("email") != "") {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		} else if (getEmail.getBody().get("email") != "") {
			return getEmail;
		}

		if (email != null) {
			Optional<Users> user = usersRepository.findByEmail(email);
			List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user.get());
			profile = lastProfile.get(0).getProfile();
		}

		Page<PostsGetDTO> postPage = postsServiceImpl.getPosts(page, profile, type, postIds);
		List<PostsGetDTO> posts = postPage.getContent();
		resultMap.put("posts", posts);

		return ResponseEntity.status(HttpStatus.OK).body(posts);
	}

	@GetMapping("/more")
	public ResponseEntity<?> getMorePosts(@RequestParam("postId") long postId, @RequestParam(value = "page", defaultValue = "0") int page, HttpServletRequest request, HttpServletResponse response) throws IOException {
		// 큐레이팅 완성되면 닉네임으로 가중치 찾아서 적용해야됨

		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();

		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = null;
		Profiles profile = null;
		if (getEmail.getStatusCode() == HttpStatus.OK && getEmail.getBody().get("email") != "") {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		} else if (getEmail.getBody().get("email") != "") {
			return getEmail;
		}

		if (email != null) {
			Optional<Users> user = usersRepository.findByEmail(email);
			List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user.get());
			profile = lastProfile.get(0).getProfile();
		}

		Optional<Posts> posts = postsRepository.findById(postId);
		if (posts.isEmpty()) {
			errorResultMap.put("error", "해당 게시글이 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		Page<PostsGetMoreDTO> commentPage = postsServiceImpl.getMorePosts(page, profile, posts.get());

		return ResponseEntity.status(HttpStatus.OK).body(commentPage.getContent());
	}

	@GetMapping("/follow")
	public ResponseEntity<?> getFollowPosts(@RequestParam(value = "page", defaultValue = "0") int page, HttpServletRequest request, HttpServletResponse response) throws IOException {
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

		List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user.get());
		if (lastProfile.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Profiles profile = lastProfile.get(0).getProfile();

		Page<PostsGetDTO> postPage = postsServiceImpl.getFollowPosts(page, profile);
		List<PostsGetDTO> posts = postPage.getContent();
		resultMap.put("posts", posts);

		return ResponseEntity.status(HttpStatus.OK).body(posts);
	}

	@GetMapping("/one")
	public ResponseEntity<?> getOnePost(@RequestParam("postId") long postId, HttpServletRequest request, HttpServletResponse response) throws IOException {
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

		List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user.get());
		if (lastProfile.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Profiles profile = lastProfile.get(0).getProfile();


		return ResponseEntity.status(HttpStatus.OK).body(postsServiceImpl.getOne(postId, profile));
	}

	@GetMapping("/search")
	public ResponseEntity<?> getSearch(
			@RequestParam(value = "type", defaultValue = "ALL") String type,
			@RequestParam(value = "keyword") String keyword,
			@RequestParam(value = "page", defaultValue = "0") int page,
			HttpServletRequest request,
			HttpServletResponse response
			)
		throws IOException {
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

		List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user.get());
		if (lastProfile.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Profiles profile = lastProfile.get(0).getProfile();

		return postsServiceImpl.getSearch(keyword, type, page, profile);
	}

	@GetMapping("/curating")
	public ResponseEntity<?> getCuratingPosts(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "postIds", required = false) List<Long> postIds, HttpServletRequest request, HttpServletResponse response) throws IOException {
		// jwtServiceImpl.getEmail()은 동기 호출이므로 Mono.defer로 감싸서 호출
		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		if (getEmail.getStatusCode() != HttpStatus.OK) {
			return getEmail;
		}
		Map<String, Object> body = getEmail.getBody();
		String email = (String) body.get("email");

		if (postIds == null || postIds.isEmpty()) {
			postIds = new ArrayList<>();
			postIds.add(-1L);
		}

		// 동기 방식으로 이메일로 사용자 조회
		Optional<Users> user = usersRepository.findByEmail(email);
		if (user.isEmpty()) {
			errorResultMap = new HashMap<>();
			errorResultMap.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user.get());
		if (lastProfile.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Profiles profile = lastProfile.get(0).getProfile();

		String uri = "https://i12c110.p.ssafy.io/curating/curating";

		List<PostsGetDTO> resultList = new ArrayList<>();

		try {
			resultList = curatingServiceImpl.getCuratingPosts(page, uri, postIds, profile);
		} catch (NullPointerException e) {
			errorResultMap.put("error", "데이터를 가져오는데에 문제가 생겼습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResultMap);
		}

		return ResponseEntity.status(HttpStatus.OK).body(resultList);
	}

	@GetMapping("/trend")
	public ResponseEntity<?> getTrendPosts(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "postIds", required = false) List<Long> postIds, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (postIds == null || postIds.isEmpty()) {
			postIds = new ArrayList<>();
			postIds.add(-1L);
		}

		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		if (getEmail.getStatusCode() != HttpStatus.OK) {
			return getEmail;
		}
		Map<String, Object> body = getEmail.getBody();
		String email = (String) body.get("email");

		// 동기 방식으로 이메일로 사용자 조회
		Optional<Users> user = usersRepository.findByEmail(email);
		if (user.isEmpty()) {
			errorResultMap = new HashMap<>();
			errorResultMap.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user.get());
		if (lastProfile.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Profiles profile = lastProfile.get(0).getProfile();

		List<PostsGetDTO> resultList = new ArrayList<>();

		try {
			resultList = postsServiceImpl.getTrendPosts(page, postIds, profile);
		} catch (NullPointerException e) {
			errorResultMap.put("error", "데이터를 가져오는데에 문제가 생겼습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResultMap);
		}

		return ResponseEntity.status(HttpStatus.OK).body(resultList);
	}
}
