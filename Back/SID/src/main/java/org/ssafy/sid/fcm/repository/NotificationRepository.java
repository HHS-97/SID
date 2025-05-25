package org.ssafy.sid.fcm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.ssafy.sid.fcm.Dto.NotificationDto;
import org.ssafy.sid.fcm.model.Notification;
import org.ssafy.sid.profiles.model.Profiles;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
//	long countByReceiverAndIsRead(Profiles profile, boolean isRead);
//	long countByReceiverInAndIsRead(List<Profiles> receivers, boolean isRead);
	 @Query("SELECT COUNT(DISTINCT n.referenceId, n.type, n.body) " +
	           "FROM Notification n " +
	           "WHERE n.receiver IN :receivers AND n.isRead = :isRead")
	    long countDistinctByReferenceIdTypeBody(@Param("receivers") List<Profiles> receivers, 
	                                            @Param("isRead") boolean isRead);
	
	Optional<Notification> findById(Long id);
//	List<Notification> findByReceiverAndIsReadOrderByCreatedAtDesc(Profiles profile, boolean isRead);
	@Query("SELECT n FROM Notification n " +
		       "WHERE n.id IN ( " +
		       "    SELECT MIN(n2.id) FROM Notification n2 " +
		       "    WHERE n2.receiver IN :receivers AND n2.isRead = :isRead " +
		       "    GROUP BY n2.referenceId, n2.type, n2.body" +
		       ") ORDER BY n.createdAt DESC")
		List<Notification> findDistinctByReceiverInAndIsRead(@Param("receivers") List<Profiles> receivers, 
		                                                     @Param("isRead") boolean isRead);

}