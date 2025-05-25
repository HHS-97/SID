package org.ssafy.sid.chat.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findTop100ByRoomIdOrderByCreatedAtAsc(String roomId);
	ChatMessage findTopByRoomIdOrderByCreatedAtDesc(String roomId);
}
