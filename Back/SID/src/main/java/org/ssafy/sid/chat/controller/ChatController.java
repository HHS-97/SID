package org.ssafy.sid.chat.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.ssafy.sid.chat.dto.ChatMessageDTO;
import org.ssafy.sid.chat.model.ChatMessage;
import org.ssafy.sid.chat.model.ChatRoom;
import org.ssafy.sid.chat.model.ChatRoomRepository;
import org.ssafy.sid.chat.service.ChatMessageServiceImpl;
import org.ssafy.sid.chat.service.ChatRoomServiceImpl;
import org.ssafy.sid.fcm.model.FcmToken;
import org.ssafy.sid.fcm.model.Notification;
import org.ssafy.sid.fcm.repository.FcmRepository;
import org.ssafy.sid.fcm.repository.NotificationRepository;
import org.ssafy.sid.fcm.service.FcmService;
import org.ssafy.sid.fcm.service.FcmServiceImpl;
import org.ssafy.sid.profiles.model.Profiles;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ChatController {
	private final SimpMessagingTemplate template;
	private final ChatRoomServiceImpl chatRoomService;
	private final ChatMessageServiceImpl chatMessageServiceImpl;
	private final ChatRoomRepository chatRoomRepository;
	private final FcmRepository fcmRepository;
	private final FcmService fcmService;
	private final FcmServiceImpl fcmServiceImpl;
	private final NotificationRepository notificationRepository;


	@MessageMapping("/{chatRoomId}/chat.sendMessage")
	public void sendMessage(@Payload @Valid ChatMessageDTO chatMessageDTO,
							@DestinationVariable String chatRoomId) {
		// 메시지를 DB와 Redis에 저장
		chatMessageServiceImpl.saveMessage(chatMessageDTO, ChatMessage.MessageType.CHAT);
		// 채팅방에 메시지 브로드캐스트 (전체 DTO 전송)
		template.convertAndSend("/topic/chat." + chatRoomId, chatMessageDTO);
		ChatRoom chatRoom = chatRoomRepository.findByRoomId(chatRoomId);
		
		Profiles receiver = null;
		Profiles sender = null;
		
		if(chatMessageDTO.getSender().equals(chatRoom.getSenderNickname())) {
			receiver = chatRoom.getReceiver();
			sender = chatRoom.getSender();
		}
		else {
			receiver = chatRoom.getSender();
			sender = chatRoom.getReceiver();
		}
		
		List<String> FcmTokens = fcmServiceImpl.getFcmTokens(receiver.getUser().getId());
		
//		System.out.println(FcmTokens);
		String type = "chat";
		if(FcmTokens != null && FcmTokens.isEmpty() == false) {
			Notification notification = Notification.builder()
					.title(sender.getNickname() + "님이 채팅을 보내셨습니다.")
					.body(chatMessageDTO.getMessage())
					.isRead(false)
					.type(type)
					.referenceId(chatRoom.getId())
					.receiver(receiver)
					.sender(sender)
					.image(sender.getProfileImage())
					.room(chatRoom.getRoomId())
					.build();
			notificationRepository.save(notification);
			for(String token : FcmTokens) {
				String title = sender.getNickname() + "님이 채팅을 보내셨습니다.";
				boolean flag = fcmServiceImpl.sendNotificationWithData(token, title, chatMessageDTO.getMessage(), type, chatRoom.getId(), chatRoom.getRoomId());
				
			}
//			Notification notification = Notification.builder()
//					.title(sender.getNickname() + "님이 채팅을 보내셨습니다.")
//					.body(chatMessageDTO.getMessage())
//					.isRead(false)
//					.type(type)
//					.referenceId(chatRoom.getId())
//					.receiver(receiver)
//					.sender(sender)
//					.image(sender.getProfileImage())
//					.room(chatRoom.getRoomId())
//					.build();
//			notificationRepository.save(notification);
		}
	}

	@MessageMapping("/{chatRoomId}/chat.addUser")
	public void addUser(@DestinationVariable String chatRoomId,
						@Payload @NotNull ChatMessage chatMessage,
						SimpMessageHeaderAccessor headerAccessor) {
		// 채팅방 입장 처리: Redis에 채팅방 메타 정보가 없으면 DB 조회 후 캐싱하고, 첫 입장이면 true 반환
		boolean isFirstJoin = chatRoomService.enterMessageRoom(chatRoomId);

		// 세션 속성에 채팅방 ID와 닉네임 저장
		headerAccessor.getSessionAttributes().put("chatRoomId", chatRoomId);
		headerAccessor.getSessionAttributes().put("nickname", chatMessage.getSender());

		if (isFirstJoin) {
			// 첫 입장 시에만 JOIN 메시지를 브로드캐스트
			template.convertAndSend("/topic/chat." + chatRoomId, chatMessage);
		} else {
			// 이미 채팅방 메타 정보가 존재하면, 저장된 메시지(히스토리)를 해당 사용자에게 전송
			List<ChatMessageDTO> history = chatMessageServiceImpl.loadMessage(chatRoomId);
			// 사용자 개별 전송: 클라이언트는 '/user/queue/history' 등을 구독해야 함
			template.convertAndSendToUser(chatMessage.getSender(), "/queue/history", history);
		}
	}
}
