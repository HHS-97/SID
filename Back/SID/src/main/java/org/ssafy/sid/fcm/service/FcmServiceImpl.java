package org.ssafy.sid.fcm.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.ssafy.sid.fcm.Dto.FcmMessageDto;
import org.ssafy.sid.fcm.Dto.FcmSendDto;
import org.ssafy.sid.fcm.model.FcmToken;
import org.ssafy.sid.fcm.repository.FcmRepository;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.model.Users;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.firebase.messaging.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FcmServiceImpl implements FcmService{

	//  sidproject-846a0 <- 내 project id
	private final FcmRepository fcmRepository;
	private final UsersRepository usersRepository;
	
	public FcmServiceImpl(FcmRepository fcmRepository, UsersRepository usersRepository) {
		this.fcmRepository = fcmRepository;
		this.usersRepository = usersRepository;
	}

	  @Transactional
	    public boolean sendNotificationWithData(String token, String title, String body, String type, Long referenceId, String room) {

//		  	System.out.println(token);
//		  	System.out.println(title);
//		  	System.out.println(body);
//		  	System.out.println(type);
//		  	System.out.println(referenceId);
	        FcmToken fcmToken = fcmRepository.findByFcmToken(token)
	                .orElseThrow(() -> new IllegalArgumentException("FCM 토큰을 찾을 수 없습니다: " + token));

	        Users user = fcmToken.getUser();
	        
//	        System.out.println(user);

//	        if (Boolean.FALSE.equals(user.getNotificationEnabled())) {
//	            log.info("알림 비활성화 상태로 알림 전송 건너뜀: " + user.getId());
//	            return false; // 알림 비활성화 시 false 반환
//	        }

	        // 메시지 구성 (data 전용)
	        Message message = Message.builder()
	                .setToken(token)
	                .putData("title", title)  // 알림 제목 nickname
	                .putData("body", body)    // 알림 내용 
	                .putData("type", type)
	                .putData("referenceId", String.valueOf(referenceId))
	                .putData("read", "false")
	                .putData("room", room)
	                .build();

//	        System.out.println("메세지는 === " + message);
	        try {
	            // 메시지 전송
	            String response = FirebaseMessaging.getInstance().send(message);
	            log.info("FCM 알림 전송 성공: " + response);
	            return true;

	        } catch (FirebaseMessagingException e) {
	            // 오류에 따라 FCM 토큰 삭제 처리
	            if (e.getMessagingErrorCode().equals(MessagingErrorCode.INVALID_ARGUMENT)) {
	                log.error("FCM 토큰이 유효하지 않습니다.", e);
	                deleteFcmToken(token);
	            } else if (e.getMessagingErrorCode().equals(MessagingErrorCode.UNREGISTERED)) {
	                log.error("FCM 토큰이 재발급 이전 토큰입니다.", e);
	                deleteFcmToken(token);
	            } else {
	                log.error("알림 전송 중 오류 발생", e);
	            }
	            return false;
	        }
	    }
	
	@Override
	@Transactional
	public void saveOrUpdateFcmToken(Long userId, String token) {
//		System.out.println("token otken " + token);
		Optional<Users> user = usersRepository.findById(userId);
		
		Optional<FcmToken> exsitToken = fcmRepository.findByFcmToken(token);
		
		if(exsitToken.isPresent()) {
			FcmToken tokenToUpdate = exsitToken.get();
//			System.out.println("이거는 savefcmtoken 부분입니다 == " + tokenToUpdate);
			if(tokenToUpdate.getUser().getId() != userId) {
//				System.out.println("이건 tokenToUpdate의 user id === " + tokenToUpdate.getUser().getId());
//				System.out.println("이건 내가 입력받은 userId === " + userId);
//				System.out.println("둘이 같지 않습니다. " );
				fcmRepository.delete(tokenToUpdate);
			}
			else {
//				System.out.println("둘이 같습니다");
				tokenToUpdate.updateTokenLastUserAt();
				return;
			}
		}
		
		FcmToken newToken = FcmToken.builder()
				.fcmToken(token)
				.user(user.get())
				.lastUserAt(LocalDateTime.now())
				.build();
		fcmRepository.save(newToken);

	}
	
	public List<String> getFcmTokens(Long userId) {
        return fcmRepository.findByUserId(userId).stream()
                .map(FcmToken::getFcmToken)
                .toList();
    }
	
	 public void deleteFcmToken(String token) {
		 fcmRepository.deleteByFcmToken(token);
	    }

}
