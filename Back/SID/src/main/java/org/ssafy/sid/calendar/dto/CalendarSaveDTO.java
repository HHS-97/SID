package org.ssafy.sid.calendar.dto;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.ssafy.sid.calendar.model.CalendarEvents;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.users.model.Users;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CalendarSaveDTO {
	@NotBlank
	@NotNull
	private String title;
	private String memo;
	@NotBlank
	@NotNull
	private String startTime;
	@NotBlank
	@NotNull
	private String endTime;
	private int alarmTime;

	public CalendarEvents toCalendarEvents(Users user, Profiles profile) {
		int computedAlarmTime = this.getAlarmTime();
		String resultAlarmTime = null;

		// alarmTime이 "시간전"으로 끝나면 상대시간으로 판단
		if (computedAlarmTime != 0) {
			try {
				// 입력받은 startTime과 저장할 alarmTime의 포맷
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

				// startTime 문자열을 LocalDateTime으로 변환
				LocalDateTime startDateTime = LocalDateTime.parse(this.getStartTime(), formatter);

				// startTime에서 hoursBefore 만큼 빼서 실제 alarmTime 계산
				LocalDateTime alarmDateTime = startDateTime.minusHours(computedAlarmTime);
				resultAlarmTime = alarmDateTime.format(formatter);
			} catch (Exception e) {
				// 파싱 에러가 발생하면 원하는 방식대로 처리 (로그 출력, 예외 던지기 등)
				e.printStackTrace();
			}
		}

		return CalendarEvents.builder()
				.title(this.title)
				.memo(this.memo)
				.profile(profile)
				.user(user)
				.alarmTime(resultAlarmTime)
				.endTime(this.endTime)
				.startTime(this.startTime)
				.build();
	}
}
