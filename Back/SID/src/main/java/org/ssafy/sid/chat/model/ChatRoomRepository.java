package org.ssafy.sid.chat.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.ssafy.sid.profiles.model.Profiles;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	@Query("SELECT cr FROM ChatRoom cr " +
			"WHERE (cr.sender = :profile AND cr.senderActive = true) " +
			"   OR (cr.receiver = :receiver AND cr.receiverActive = true)")
	List<ChatRoom> findByProfileOrReceiver(@Param("profile") Profiles profile, @Param("receiver") Profiles receiver);

	ChatRoom findByIdAndSenderOrIdAndReceiver(Long id, Profiles sender, Long id1, Profiles receiver);

	ChatRoom findBySenderAndReceiver(Profiles sender, Profiles receiver);

	@Query("SELECT cr FROM ChatRoom cr " +
			"WHERE (cr.roomId = :roomId AND cr.sender = :sender AND cr.senderActive = true) " +
			"   OR (cr.roomId = :roomId1 AND cr.receiver = :receiver AND cr.receiverActive = true)")
	ChatRoom findByRoomIdAndSenderOrRoomIdAndReceiver(
			@Param("roomId") String roomId,
			@Param("sender") Profiles sender,
			@Param("roomId1") String roomId1,
			@Param("receiver") Profiles receiver);

	ChatRoom findByRoomId(String roomId);
}
