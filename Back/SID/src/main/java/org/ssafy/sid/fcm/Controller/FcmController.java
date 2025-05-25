package org.ssafy.sid.fcm.Controller;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.internal.build.AllowSysOut;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ssafy.sid.fcm.Dto.FcmTokenRequestDto;
import org.ssafy.sid.fcm.Dto.NotificationDto;
import org.ssafy.sid.fcm.Dto.ReceiverDto;
import org.ssafy.sid.fcm.Dto.SenderDto;
import org.ssafy.sid.fcm.model.Notification;
import org.ssafy.sid.fcm.repository.NotificationRepository;
import org.ssafy.sid.fcm.service.FcmService;
import org.ssafy.sid.follow.model.Follows;
import org.ssafy.sid.follow.model.FollowsRepository;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.lastprofiles.model.LastProfilesRepository;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.profiles.model.ProfilesRepository;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.jwt.service.JwtServiceImpl;
import org.ssafy.sid.users.model.Users;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/notification")
public class FcmController {

    private final FcmService fcmService;
    private final JwtServiceImpl jwtServiceImpl;
    private final UsersRepository usersrepo;
    private final NotificationRepository notificationRepository;
    private final ProfilesRepository profileRepository;
    private final LastProfilesRepository lastProfilesRepository;
    private final FollowsRepository followRepository;
    

    public FcmController(FcmService fcmService, JwtServiceImpl jwtServiceImpl, UsersRepository usersrepo
    		, NotificationRepository notificationRepository, ProfilesRepository profileRepository
    		,LastProfilesRepository lastProfilesRepository, FollowsRepository followRepository) {
        this.fcmService = fcmService;
        this.jwtServiceImpl = jwtServiceImpl;
        this.usersrepo = usersrepo;
        this.notificationRepository = notificationRepository;
        this.profileRepository = profileRepository;
        this.lastProfilesRepository =lastProfilesRepository;
        this.followRepository = followRepository;
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<String> registerFcmToken(@RequestParam("fcmToken") String fcmtoken,
    		HttpServletRequest request) {
    	// 이거 왜 requestbody 안되는지 나중에 해결하기
//    	System.out.println("Fcm token 입니다." + fcmtoken);
    	
    	FcmTokenRequestDto requestDto = new FcmTokenRequestDto();
    	requestDto.setFcmToken(fcmtoken);
    	
    	String email = null;
		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmailLogout(request);
		if (getEmail.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		}
		Optional<Users> user = usersrepo.findByEmail(email);
		
		Long user_id = null;
		
		if(user.isPresent()) {
			user_id = user.get().getId();
		}
		
//		System.out.println("유저에 대한 정보입니다 == " + user_id);


		fcmService.saveOrUpdateFcmToken(user_id, requestDto.getFcmToken());

    	return ResponseEntity.ok("FCM 토큰이 성공적으로 등록되었습니다.");
    }
    
    @PostMapping("/saveNoti")
    public ResponseEntity<String> saveNotification(@RequestParam("title") String title, @RequestParam("body") String body,
    		@RequestParam("type") String type, @RequestParam("referenceId") String referenceId, @RequestParam("image") String image
    		,HttpServletRequest request){
    	
//    	System.out.println("여기는 Save Noti === " + title);
//    	System.out.println("여기는 Save Noti === " + body);
//    	System.out.println("여기는 Save Noti === " + type);
//    	System.out.println("여기는 Save Noti === " + referenceId);
//    	System.out.println("Save Noti === " + image);
    	Long referId = Long.parseLong(referenceId);
    	
    	String email = null;
		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmailLogout(request);
		if (getEmail.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> body2 = getEmail.getBody();
			email = (String) body2.get("email");
		}
		Optional<Users> user = usersrepo.findByEmail(email);
		
		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());
		Profiles profile = lastProfiles.get(0).getProfile();
		// 이게 보내는 사람의 프로필이다.
		
		Long user_id = null;
		
		if(user.isPresent()) {
			user_id = user.get().getId();
		}
		
//		System.out.println("유저에 대한 정보입니다 == " + user_id);
//		System.out.println("이거는 profile 정보" + profile);
		
		List<Follows> followList = followRepository.findByFollowing(profile);
		
		for(Follows follow : followList) {
			Notification notification = Notification.builder()
	    			.title(title)
	    			.body(body)
	    			.isRead(false)
	    			.type(type)
	    			.referenceId(referId)
	    			.receiver(follow.getFollower())
	    			.sender(profile)
	    			.image(image)
	    			.build();
	    	notificationRepository.save(notification);
		}

    	return ResponseEntity.ok("알림이 저장 되었습니다.");
    }
    
    @GetMapping("/countNoti")
    public ResponseEntity<?> countNoti(HttpServletRequest request) {
        String email = null;
        ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmailLogout(request);
        
        if (getEmail.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> body = getEmail.getBody();
            email = (String) body.get("email");
        }
        
        Optional<Users> user = usersrepo.findByEmail(email);
        
        
        if (user.isPresent()) {
            List<LastProfiles> lastprofile = lastProfilesRepository.findByUser(user.get());
//            System.out.println("이건 라스트 프로필임 == " + lastprofile.get(0));
            List<Profiles> profileList = profileRepository.findByUser(user.get());
            
            // 알림의 개수 계산
            long count = notificationRepository.countDistinctByReferenceIdTypeBody(profileList, false);
//            System.out.println("알림의 개수는? === " + count);
            
            // 응답 본문에 count 값을 포함하여 반환
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }
    }
    
    @GetMapping("/summaryNoti")
    public ResponseEntity<?> SummaryNoti(HttpServletRequest request) {
//    	System.out.println("summaryNoti는 들어와야지");
    	String email = null;
        ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmailLogout(request);
        
        if (getEmail.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> body = getEmail.getBody();
            email = (String) body.get("email");
        }
        
        Optional<Users> user = usersrepo.findByEmail(email);
//        System.out.println("SummaryNoti === " + user);
        
        if(user.isPresent()) {
        	List<LastProfiles> lastprofile = lastProfilesRepository.findByUser(user.get());
//        	System.out.println("Summary Last Profile == " + lastprofile);
        	List<Profiles> profileList = profileRepository.findByUser(user.get());
        	
        	List<Notification> notifications = notificationRepository.findDistinctByReceiverInAndIsRead(profileList, false);
        	List<NotificationDto> notificationDtos = new ArrayList<>();
        	for(Notification noti : notifications) {
        		
        		SenderDto senderDto = SenderDto.builder()
        				.senderId(noti.getSender().getId())
        				.senderNickname(noti.getSender().getNickname())
        				.senderProfileImage(noti.getImage())
        				.build();
        		
        		ReceiverDto receiverDto = ReceiverDto.builder()
        				.receiverId(noti.getReceiver().getId())
        				.receiverNickname(noti.getReceiver().getNickname())
        				.build();
        		
//        		System.out.println("일단 정보는 들어왔네요 이건 == "  + noti);
        		NotificationDto notifiDto = NotificationDto.builder()
        			.notificationId(noti.getId())
        			.title(noti.getTitle())
        			.body(noti.getBody())
        			.isRead(noti.getIsRead())
        			.createdAt(calculateTimeDifference(noti.getCreatedAt()))
        			.type(noti.getType())
        			.referenceId(noti.getReferenceId())
        			.sender(senderDto)
        			.receiver(receiverDto)
        			.room(noti.getRoom())
        			.build();
        		notificationDtos.add(notifiDto);
        	}
        	return ResponseEntity.ok(notificationDtos);
        }
        else {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }

    }
    @PostMapping("/markAsRead")
    public ResponseEntity<?> markNotificationAsRead(@RequestParam("id") Long id) {
//        System.out.println(id);
        return notificationRepository.findById(id)
            .map(notification -> {
                notification.setRead(true);
                notificationRepository.save(notification);
                return ResponseEntity.ok("Notification marked as read");
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found"));
    }
    
    @PostMapping("/markAllAsRead")
    public ResponseEntity<?> markNotificationAllAsRead(HttpServletRequest request) {
    	String email = null;
        ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmailLogout(request);
        
        if (getEmail.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> body = getEmail.getBody();
            email = (String) body.get("email");
        }
        
        Optional<Users> user = usersrepo.findByEmail(email);
        if (user.isPresent()) {
            List<LastProfiles> lastprofile = lastProfilesRepository.findByUser(user.get());
            List<Profiles> profileList = profileRepository.findByUser(user.get());
            List<Notification> notis = notificationRepository.findDistinctByReceiverInAndIsRead(profileList, false);
            
            // 모든 알림의 isRead 상태를 true로 변경
            for (Notification notification : notis) {
                notification.setRead(true);
            }
            
            // 변경된 알림들을 저장
            notificationRepository.saveAll(notis);
            
            return ResponseEntity.ok("All notifications marked as read");
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");       
    }
    
    private String calculateTimeDifference(String createdAtStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime createdAt;
        try {
            createdAt = LocalDateTime.parse(createdAtStr, formatter);
        } catch (DateTimeParseException e) {
            return "시간 정보 없음";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        if (days > 0) {
            return days + "일 전";
        } else if (hours > 0) {
            return hours + "시간 전";
        } else {
            return minutes + "분 전";
        }
    }
    
}
