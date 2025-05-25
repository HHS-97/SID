package org.ssafy.sid.calendar.dto;

import lombok.*;
import org.ssafy.sid.profiles.model.Profiles;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CalendarDeleteDTO {
	private long scheduleId;
}
