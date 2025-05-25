package org.ssafy.sid.chat.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.ssafy.sid.chat.dto.ChatMessageDTO;
import org.ssafy.sid.chat.dto.ChatMessageRequestDTO;
import org.ssafy.sid.chat.dto.ChatRoomDTO;
import org.ssafy.sid.chat.dto.ChatRoomResponseDTO;
import org.ssafy.sid.chat.model.ChatMessage;
import org.ssafy.sid.chat.model.ChatMessageRepository;
import org.ssafy.sid.chat.service.ChatRoomServiceImpl;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.lastprofiles.model.LastProfilesRepository;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.jwt.service.JwtServiceImpl;
import org.ssafy.sid.users.model.Users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestMapping("/api/chatroom")
@RequiredArgsConstructor
@RestController
public class ChatRoomController {
	private final ChatRoomServiceImpl chatRoomService;
	private final ChatRoomServiceImpl chatRoomServiceImpl;
	private final LastProfilesRepository lastProfilesRepository;
	private final JwtServiceImpl jwtServiceImpl;
	private final UsersRepository usersRepository;
	private final ChatMessageRepository chatMessageRepository;

//	@GetMapping
//	public List<ChatRoomResponse> findAll() {
//		return chatRoomService.findAll();
//	}
//
//	@PostMapping
//	public ResponseEntity<?> addChatRoom(@RequestBody @Valid ChatRoomSaveRequest chatRoomSaveRequest) {
//		Map<String, Object> result = new HashMap<String, Object>();
//		ChatRoom chatRoom = chatRoomService.save(chatRoomSaveRequest.getName());
//		result.put("chatRoomId", chatRoom.getId());
//		return ResponseEntity.status(HttpStatus.CREATED).body(result);
//	}


	// 쪽지방 생성
	@PostMapping
	public ResponseEntity<?> createRoom(@RequestBody ChatMessageRequestDTO chatMessageRequestDTO, HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> errorResult = new HashMap<String, Object>();

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
			errorResult.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResult);
		}
		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());

		if (lastProfiles.isEmpty()) {
			result.put("error", "프로필접속 기록이 없습니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResult);
		}

		if (chatMessageRequestDTO != null && chatMessageRequestDTO.getReceiver().equals(lastProfiles.get(0).getProfile().getNickname())) {
			result.put("error", "스스로와 채팅을 할 수 는 없습니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
		}

		chatRoomServiceImpl.save(chatMessageRequestDTO, lastProfiles.get(0).getProfile());

		result.put("message", "채팅방이 생성됐습니다.");
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	// 사용자 관련 쪽지방 전체 조회
	@GetMapping
	public ResponseEntity<?> findAllRoomByUser(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> errorResult = new HashMap<String, Object>();

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
			errorResult.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResult);
		}

		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());

		if (lastProfiles.isEmpty()) {
			result.put("error", "프로필접속 기록이 없습니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResult);
		}

		List<ChatRoomResponseDTO> chatRoomList = chatRoomServiceImpl.findAllRoomByProfile(lastProfiles.get(0).getProfile());
		if (chatRoomList.isEmpty()) {
			result.put("error", "방이 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResult);
		}

		result.put("chatRoomList", chatRoomList);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	// 사용자 관련 쪽지방 선택 조회
	@GetMapping("/{roomId}")
	public ResponseEntity<?> findRoom(@PathVariable String roomId, HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> errorResult = new HashMap<String, Object>();

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
			errorResult.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResult);
		}

		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());

		if (lastProfiles.isEmpty()) {
			result.put("error", "프로필접속 기록이 없습니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResult);
		}

		ChatRoomDTO chatRoomDTO = chatRoomServiceImpl.findRoom(roomId, lastProfiles.get(0).getProfile());

		if (chatRoomDTO == null) {
			result.put("error", "방이 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResult);
		}

		result.put("chatRoom", chatRoomDTO);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

//	// 쪽지방 삭제
//	@DeleteMapping("/{id}")
//	public ResponseEntity<?> deleteRoom(@PathVariable Long id, HttpServletRequest request) {
//		Map<String, Object> result = new HashMap<String, Object>();
//		Map<String, Object> errorResult = new HashMap<String, Object>();
//
//		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
//		String email = "";
//		if (getEmail.getStatusCode() == HttpStatus.OK) {
//			Map<String, Object> body = getEmail.getBody();
//			email = (String) body.get("email");
//		} else {
//			return getEmail;
//		}
//		Optional<Users> user = usersRepository.findByEmail(email);
//
//		if (user.isEmpty()) {
//			errorResult.put("error", "존재하지 않는 유저입니다.");
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResult);
//		}
//
//		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());
//
//		if (lastProfiles.isEmpty()) {
//			result.put("error", "프로필접속 기록이 없습니다.");
//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResult);
//		}
//
//		chatRoomServiceImpl.deleteRoom(id, lastProfiles.get(0).getProfile());
//
//		result.put("message", "채팅방을 삭제했습니다.");
//		result.put("id", id);
//		return ResponseEntity.status(HttpStatus.OK).body(result);
//	}

	@PostMapping("/{roomId}/leave")
	public ResponseEntity<?> leaveRoom(@PathVariable String roomId, HttpServletRequest request) {
		Map<String, Object> result = new HashMap<>();
		Map<String, Object> errorResult = new HashMap<>();

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
			errorResult.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResult);
		}

		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());

		if (lastProfiles.isEmpty()) {
			errorResult.put("error", "프로필접속 기록이 없습니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResult);
		}

		// 명시적으로 채팅방에서 퇴장 처리
		chatRoomService.exit(roomId, lastProfiles.get(0).getProfile().getNickname());

		result.put("message", "채팅방을 나갔습니다.");
		result.put("roomId", roomId);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{roomId}/messages")
	public ResponseEntity<List<ChatMessageDTO>> getMessages(@PathVariable String roomId) {
		// roomId에 해당하는 모든 채팅 메시지를 생성 시간 기준 오름차순으로 조회합니다.
		List<ChatMessage> messages = chatMessageRepository.findTop100ByRoomIdOrderByCreatedAtAsc(roomId);
		// 엔티티를 DTO로 변환 (변환 메서드는 필요에 따라 구현)
		List<ChatMessageDTO> dtos = messages.stream()
				.map(ChatMessageDTO::fromEntity)
				.collect(Collectors.toList());

//		System.out.println(dtos.toString());
		return ResponseEntity.status(HttpStatus.OK).body(dtos);
	}
}
