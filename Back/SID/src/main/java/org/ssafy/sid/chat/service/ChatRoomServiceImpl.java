package org.ssafy.sid.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.ssafy.sid.chat.dto.*;
import org.ssafy.sid.chat.model.ChatMessage;
import org.ssafy.sid.chat.model.ChatMessageRepository;
import org.ssafy.sid.chat.model.ChatRoom;
import org.ssafy.sid.chat.model.ChatRoomRepository;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.profiles.model.ProfilesRepository;

import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatRoomServiceImpl implements ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;

	private static final String CHAT_ROOMS = "CHAT_ROOM";
	private final RedisTemplate<String, Object> redisTemplate;
	private final ChatMessageRepository chatMessageRepository;
	private final ProfilesRepository profilesRepository;
	private HashOperations<String, String, ChatRoomDTO> opsHashMessageRoom;
	private final ObjectMapper objectMapper;

	@PersistenceContext
	private EntityManager entityManager;

	@PostConstruct
	private void init() {
		opsHashMessageRoom = redisTemplate.opsForHash();
	}

	@Override
	@Transactional
	public ChatRoomResponseDTO save(ChatMessageRequestDTO chatMessageRequestDto, Profiles profile) {
		Optional<Profiles> receivers = profilesRepository.findByNickname(chatMessageRequestDto.getReceiver());

		ChatRoom chatRoom = chatRoomRepository.findBySenderAndReceiver(profile, receivers.get());

		// 처음 쪽지방 생성 또는 이미 생성된 쪽지방이 아닌 경우
		if (chatRoom == null || (!chatRoom.isReceiverActive() && !chatRoom.isSenderActive())) {
			ChatRoomDTO chatRoomDTO = ChatRoomDTO.create(profile, receivers.get());
			opsHashMessageRoom.put(CHAT_ROOMS, chatRoomDTO.getRoomId(), chatRoomDTO);      // redis hash 에 쪽지방 저장해서, 서버간 채팅방 공유
			chatRoom = chatRoomRepository.save(new ChatRoom(
					chatRoomDTO.getId(),
					chatRoomDTO.getRoomName(),
					chatRoomDTO.getSenderNickname(),
					chatRoomDTO.getRoomId(),
					chatRoomDTO.getReceiverNickname(),
					chatRoomDTO.getSender(),
					chatRoomDTO.getReceiver()
					));

			return new ChatRoomResponseDTO(chatRoom);
			// 이미 생성된 쪽지방인 경우
		} else {
			return new ChatRoomResponseDTO(chatRoom.getRoomId());
		}
	}

	@Override
	@Transactional
	// 사용자 관련 쪽지방 전체 조회
	public List<ChatRoomResponseDTO> findAllRoomByProfile(Profiles profile) {
		List<ChatRoom> chatRooms = chatRoomRepository.findByProfileOrReceiver(profile, profile);      // sender & receiver 모두 해당 쪽지방 조회 가능 (1:1 대화)

		List<ChatRoomResponseDTO> chatRoomDTOs = new ArrayList<>();

		for (ChatRoom chatRoom : chatRooms) {
			//  user 가 sender 인 경우
			if (profile.equals(chatRoom.getSender())) {
				ChatRoomResponseDTO chatRoomDTO = new ChatRoomResponseDTO(chatRoom);

				// 8. 가장 최신 메시지 & 생성 시간 조회
				ChatMessage latestMessage = chatMessageRepository.findTopByRoomIdOrderByCreatedAtDesc(chatRoom.getRoomId());
				if (latestMessage != null) {
					chatRoomDTO.setLatestMessageCreatedAt(latestMessage.getCreatedAt());
					chatRoomDTO.setLatestMessageContent(latestMessage.getMessage());
				}

				chatRoomDTOs.add(chatRoomDTO);
				// profile이 receiver 인 경우
			} else {
				ChatRoomResponseDTO chatRoomDTO = new ChatRoomResponseDTO(chatRoom);

				// 가장 최신 메시지 & 생성 시간 조회
				ChatMessage latestMessage = chatMessageRepository.findTopByRoomIdOrderByCreatedAtDesc(chatRoom.getRoomId());
				if (latestMessage != null) {
					chatRoomDTO.setLatestMessageCreatedAt(latestMessage.getCreatedAt());
					chatRoomDTO.setLatestMessageContent(latestMessage.getMessage());
				}

				chatRoomDTOs.add(chatRoomDTO);
			}
		}

		return chatRoomDTOs;
	}

	@Override
	@Transactional
	// 사용자 관련 쪽지방 선택 조회
	public ChatRoomDTO findRoom(String roomId, Profiles profile) {
		ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId);

		// sender & receiver 모두 messageRoom 조회 가능
		chatRoom = chatRoomRepository.findByRoomIdAndSenderOrRoomIdAndReceiver(roomId, profile, roomId, profile);
		if (chatRoom == null) {
			return null;
		}

		ChatRoomDTO chatRoomDTO = new ChatRoomDTO(
				chatRoom.getId(),
				chatRoom.getRoomName(),
				chatRoom.getRoomId(),
				chatRoom.getSenderNickname(),
				chatRoom.getReceiverNickname(),
				chatRoom.getSender(),
				chatRoom.getReceiver()
				);

		return chatRoomDTO;
	}

//	@Override
//	@Transactional
//	// 쪽지방 삭제
//	public MsgResponseDTO deleteRoom(Long id, Profiles profile) {
//		ChatRoom chatRoom = chatRoomRepository.findByIdAndProfileOrIdAndReceiver(id, profile, id, profile.getNickname());
//
//		// sender 가 삭제할 경우
//		if (profile.getNickname().equals(chatRoom.getSender())) {
//			chatRoomRepository.delete(chatRoom);
//			opsHashMessageRoom.delete(CHAT_ROOMS, chatRoom.getRoomId());
//			// receiver 가 삭제할 경우
//		} else if (profile.getNickname().equals(chatRoom.getReceiver())) {
//			chatRoom.setReceiver("Not_Exist_Receiver");
//			chatRoomRepository.save(chatRoom);
//		}
//
//		return new MsgResponseDTO("채팅방을 삭제했습니다.", HttpStatus.OK.value());
//	}

	@Override
	@Transactional
// 채팅방 입장: Redis에 메타 정보가 없으면 DB에서 조회 후 캐싱하고 true (첫 입장)을 반환, 있으면 false 반환
	public boolean enterMessageRoom(String roomId) {
		if (roomId == null) {
			log.error("채팅방 ID가 null입니다.");
			throw new IllegalArgumentException("채팅방 ID는 null일 수 없습니다.");
		}

		// Redis의 "CHAT_ROOM" 해시에서 해당 roomId의 채팅방 정보를 조회합니다.
		ChatRoomDTO chatRoomDTO = opsHashMessageRoom.get(CHAT_ROOMS, roomId);
//		System.out.println("채팅방 ID: " + roomId);

		if (chatRoomDTO == null) {
			// 채팅방 정보가 없다면, DB에서 조회 후 Redis에 저장합니다.
			log.warn("채팅방 {} 정보가 Redis에 존재하지 않습니다. DB에서 조회합니다.", roomId);
			ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId);
			if (chatRoom != null) {
				chatRoomDTO = new ChatRoomDTO(
						chatRoom.getId(),
						chatRoom.getRoomName(),
						chatRoom.getRoomId(),
						chatRoom.getSenderNickname(),
						chatRoom.getReceiverNickname(),
						chatRoom.getSender(),
						chatRoom.getReceiver()
				);
				// DB에서 조회한 정보를 Redis에 캐싱합니다.
				opsHashMessageRoom.put(CHAT_ROOMS, roomId, chatRoomDTO);
				log.info("채팅방 {} 정보를 DB에서 조회하여 Redis에 업데이트했습니다.", roomId);
				return true; // 첫 입장
			} else {
				log.error("채팅방 {} 정보가 DB에도 존재하지 않습니다.", roomId);
				throw new IllegalArgumentException("채팅방 정보를 찾을 수 없습니다.");
			}
		} else {
			// 채팅방 정보가 이미 Redis에 존재하는 경우, (필요시 추가 정보 업데이트 후) 다시 캐싱
			opsHashMessageRoom.put(CHAT_ROOMS, roomId, chatRoomDTO);
			log.info("채팅방 {} 정보를 Redis에 업데이트했습니다.", roomId);
			return false;
		}
	}

	@Override
	@Transactional
	// Redis에서 채팅방 메타 정보 조회
	public ChatRoomDTO getChatRoomMeta(String roomId) {
		return opsHashMessageRoom.get(CHAT_ROOMS, roomId);
	}

	@Override
	@Transactional
	public void exit(String roomId, String nickname) {
		// roomId로 채팅방을 조회
		ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId);
		if (chatRoom == null) {
			log.warn("채팅방 {}이(가) DB에 존재하지 않습니다.", roomId);
			return;
		}

		// 사용자가 sender인지 receiver인지 확인 후, 해당 활성 상태를 false로 변경
		if (nickname.equals(chatRoom.getSenderNickname())) {
			chatRoom.setSenderActive(false);
			chatRoomRepository.save(chatRoom);
			log.info("Sender {}가 채팅방 {}에서 나갔습니다.", nickname, roomId);
		} else if (nickname.equals(chatRoom.getReceiverNickname())) {
			chatRoom.setReceiverActive(false);
			chatRoomRepository.save(chatRoom);
			log.info("Receiver {}가 채팅방 {}에서 나갔습니다.", nickname, roomId);
		} else {
			log.warn("사용자 {}는 채팅방 {}의 참여자가 아닙니다.", nickname, roomId);
			return;
		}

		// 두 참가자 모두 활성 상태가 false라면 chatRoom의 delete() 메서드를 실행
		if (!chatRoom.isSenderActive() && !chatRoom.isReceiverActive()) {
			chatRoom.delete();  // 채팅방의 논리 삭제 수행 (isDeleted = true)
			chatRoomRepository.save(chatRoom);
			log.info("채팅방 {}의 delete() 메서드를 실행했습니다.", roomId);

			// 삭제된 채팅방인 경우 Redis의 메타 정보도 삭제
			opsHashMessageRoom.delete(CHAT_ROOMS, roomId);
			log.info("채팅방 {}의 Redis 메타 정보가 삭제되었습니다.", roomId);
		} else {
			// 삭제되지 않은 경우, 필요 시 Redis 메타 정보 업데이트
			Object redisData = opsHashMessageRoom.get(CHAT_ROOMS, roomId);
			if (redisData != null) {
				ChatRoomDTO chatRoomDTO = objectMapper.convertValue(redisData, ChatRoomDTO.class);
				opsHashMessageRoom.put(CHAT_ROOMS, roomId, chatRoomDTO);
				log.info("채팅방 {}의 Redis 메타 정보가 업데이트되었습니다.", roomId);
			}
		}

		// 영속성 컨텍스트 초기화: flush 후 clear 호출하여 캐시 초기화
		entityManager.flush();
		entityManager.clear();
	}
}
