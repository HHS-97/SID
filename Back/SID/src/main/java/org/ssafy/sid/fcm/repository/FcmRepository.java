package org.ssafy.sid.fcm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.ssafy.sid.fcm.model.FcmToken;
import java.util.List;

public interface FcmRepository extends JpaRepository<FcmToken, Long> {
	
	Optional<FcmToken> findByFcmToken(String token);
	void deleteByFcmToken(String fcmtoken);
	List<FcmToken> findByUserId(Long userId);
	
	@Query("SELECT ft.id FROM FcmToken ft WHERE ft.fcmToken = :token")
	Optional<Long> findFcmTokenIdByFcmToken(@Param("token") String token);
}
