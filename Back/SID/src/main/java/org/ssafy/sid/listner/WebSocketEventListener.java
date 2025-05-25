package org.ssafy.sid.listner;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.ssafy.sid.chat.model.ChatMessage;
import org.ssafy.sid.chat.service.ChatRoomServiceImpl;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
	private final SimpMessageSendingOperations messagingTemplate;

	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

		String nickname = (String) headerAccessor.getSessionAttributes().get("nickname");
		String chatRoomId = (String) headerAccessor.getSessionAttributes().get("chatRoomId");
		Boolean explicitExit = (Boolean) headerAccessor.getSessionAttributes().get("explicitExit");

		// 명시적 퇴장(즉, 사용자가 "나가기" 버튼을 눌러 explicitExit 플래그가 true로 설정된 경우)에만 leave 메시지를 전송
		if (nickname != null && chatRoomId != null && Boolean.TRUE.equals(explicitExit)) {
			ChatMessage chatMessage = new ChatMessage(
					nickname,
					chatRoomId,
					nickname + "님이 채팅방을 나갔습니다.",
					ChatMessage.MessageType.LEAVE
			);
			messagingTemplate.convertAndSend("/topic/chat." + chatRoomId, chatMessage);

			headerAccessor.getSessionAttributes().remove("explicitExit");
		}
	}

}
