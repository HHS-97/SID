package org.ssafy.sid.calendar.dto;

import lombok.*;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CalendarUpdateDTO {
	private long scheduleId;

	private String title;

	@Builder.Default
	private String memo = null;

	@Builder.Default
	private String startTime = null;

	@Builder.Default
	private String endTime = null;

	@Builder.Default
	private int alarmTime = 0;


}
