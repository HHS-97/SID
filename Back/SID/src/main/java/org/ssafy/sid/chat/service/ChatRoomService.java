package org.ssafy.sid.chat.service;

import org.ssafy.sid.chat.dto.*;
import org.ssafy.sid.profiles.model.Profiles;

import java.util.List;

public interface ChatRoomService {
	ChatRoomResponseDTO save(ChatMessageRequestDTO chatMessageRequestDto, Profiles profile);
	List<ChatRoomResponseDTO> findAllRoomByProfile(Profiles profile);
	ChatRoomDTO findRoom(String roomId, Profiles profile);
//	MsgResponseDTO deleteRoom(Long id, Profiles profile);
	boolean enterMessageRoom(String roomId);
	ChatRoomDTO getChatRoomMeta(String roomId);
	void exit(String roomId, String nickname);
}
