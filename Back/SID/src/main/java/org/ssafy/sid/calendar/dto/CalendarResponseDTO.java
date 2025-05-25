package org.ssafy.sid.calendar.dto;

import lombok.*;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CalendarResponseDTO {
	private long scheduleId;
	private String nickname;
	private String title;
	private String start;
	private String end;
	private String memo;
	private String alarmTime;
}
