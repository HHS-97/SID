package org.ssafy.sid.chat.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;
import org.ssafy.sid.chat.dto.ChatMessageDTO;
import org.ssafy.sid.chat.model.ChatMessage;
import org.ssafy.sid.chat.model.ChatMessageRepository;
import org.ssafy.sid.chat.model.ChatRoom;
import org.ssafy.sid.chat.model.ChatRoomRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

	private final RedisTemplate<String, ChatMessageDTO> redisTemplateMessage;
	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;

	// 대화 저장
	@Override
	@Transactional
	public void saveMessage(ChatMessageDTO chatMessageDto, ChatMessage.MessageType type) {
		// 채팅방 조회 및 활성 상태 확인
		ChatRoom chatRoom = chatRoomRepository.findByRoomId(chatMessageDto.getRoomId());
		if (chatRoom == null) {
			throw new IllegalStateException("채팅방이 존재하지 않습니다.");
		}
		// 두 참여자가 모두 active여야 메시지 전송이 가능함
		if (!chatRoom.isSenderActive() || !chatRoom.isReceiverActive()) {
			throw new IllegalStateException("채팅방 참여자 중 한 명이 퇴장하여 메시지를 보낼 수 없습니다.");
		}

		// DB 저장
		ChatMessage chatMessage = new ChatMessage(
				chatMessageDto.getSender(),
				chatMessageDto.getRoomId(),
				chatMessageDto.getMessage(),
				type
		);
		chatMessageRepository.save(chatMessage);

		// Redis 직렬화 설정
		redisTemplateMessage.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatMessage.class));

		// Redis에 메시지 저장
		redisTemplateMessage.opsForList().rightPush(chatMessageDto.getRoomId(), chatMessageDto);

		// 저장된 메시지를 1분 후 만료 (필요에 따라 만료 시간을 조정)
		redisTemplateMessage.expire(chatMessageDto.getRoomId(), 1, TimeUnit.MINUTES);
	}

	// 6. 대화 조회 - Redis & DB
	@Override
	@Transactional
	public List<ChatMessageDTO> loadMessage(String roomId) {
		List<ChatMessageDTO> messageList = new ArrayList<>();

		// Redis 에서 해당 채팅방의 메시지 100개 가져오기
		List<ChatMessageDTO> redisMessageList = redisTemplateMessage.opsForList().range(roomId, 0, 99);

		// 4. Redis 에서 가져온 메시지가 없다면, DB 에서 메시지 100개 가져오기
		if (redisMessageList.isEmpty()) {
			// 5.
			List<ChatMessage> dbMessageList = chatMessageRepository.findTop100ByRoomIdOrderByCreatedAtAsc(roomId);

			for (ChatMessage message : dbMessageList) {
				ChatMessageDTO chatMessageDTO = new ChatMessageDTO(message);
				messageList.add(chatMessageDTO);
				redisTemplateMessage.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatMessage.class));      // 직렬화
				redisTemplateMessage.opsForList().rightPush(roomId, chatMessageDTO);                                // redis 저장
			}
		} else {
			// 7.
			messageList.addAll(redisMessageList);
		}

		return messageList;
	}
}
