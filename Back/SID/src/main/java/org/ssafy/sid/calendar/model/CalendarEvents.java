package org.ssafy.sid.calendar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.ssafy.sid.calendar.dto.CalendarDeleteDTO;
import org.ssafy.sid.calendar.dto.CalendarUpdateDTO;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.users.model.Users;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CalendarEvents {
	@Id
	@Column(columnDefinition = "INT(11) UNSIGNED")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", columnDefinition = "INT(11) UNSIGNED", nullable = false)
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "profile_id", columnDefinition = "INT(11) UNSIGNED", nullable = false, updatable = false)
	private Profiles profile;

	@Column(length = 32, nullable = false)
	@NotBlank
	private String title;

	@Column(columnDefinition = "TEXT")
	private String memo;

	@CreatedDate
	@Column(updatable = false, nullable = false, columnDefinition="CHAR(19)")
	private String createdAt;

	@LastModifiedDate
	@Column(nullable = false, columnDefinition="CHAR(19)")
	private String updatedAt;

	@Column(nullable = false, columnDefinition="CHAR(19)")
	private String startTime;

	@Column(nullable = false, columnDefinition="CHAR(19)")
	private String endTime;

	@Column(columnDefinition="CHAR(19)")
	private String alarmTime;

	@Builder.Default
	@Column(columnDefinition = "TINYINT(1)")
	private Boolean isDeleted = false;

	@PrePersist
	public void onPrePersist(){
		this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		this.updatedAt = this.createdAt;
	}
	@PreUpdate
	public void onPreUpdate(){
		this.updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	public void update(CalendarUpdateDTO dto) {
		int computedAlarmTime = dto.getAlarmTime();
		String resultAlarmTime = null;

		// alarmTime이 "시간전"으로 끝나면 상대시간으로 판단
		if (computedAlarmTime != 0) {
			try {
				// 입력받은 startTime과 저장할 alarmTime의 포맷
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

				// startTime 문자열을 LocalDateTime으로 변환
				LocalDateTime startDateTime = LocalDateTime.parse(dto.getStartTime(), formatter);

				// startTime에서 hoursBefore 만큼 빼서 실제 alarmTime 계산
				LocalDateTime alarmDateTime = startDateTime.minusHours(computedAlarmTime);
				resultAlarmTime = alarmDateTime.format(formatter);
			} catch (Exception e) {
				// 파싱 에러가 발생하면 원하는 방식대로 처리 (로그 출력, 예외 던지기 등)
				e.printStackTrace();
			}
		}

		if (dto.getTitle() != null) {
			this.title = dto.getTitle();
		}
		if (dto.getMemo() != null) {
			this.memo = dto.getMemo();
		}
		if (dto.getStartTime() != null) {
			this.startTime = dto.getStartTime();
		}
		if (dto.getEndTime() != null) {
			this.endTime = dto.getEndTime();
		}
		this.alarmTime = resultAlarmTime;
	}
}
