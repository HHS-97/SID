package org.ssafy.sid.calendar.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.ssafy.sid.calendar.dto.CalendarDeleteDTO;
import org.ssafy.sid.calendar.dto.CalendarSaveDTO;
import org.ssafy.sid.calendar.dto.CalendarUpdateDTO;
import org.ssafy.sid.users.model.Users;

import java.util.List;
import java.util.Map;

public interface CalendarService {
	ResponseEntity<Map<String, Object>> createCalendar(CalendarSaveDTO calendarSaveDTO, HttpServletRequest request);
	ResponseEntity<Map<String, Object>> updateCalendar(CalendarUpdateDTO calendarUpdateDTO, HttpServletRequest request);
	ResponseEntity<Map<String, Object>> deleteCalendar(CalendarDeleteDTO calendarDeleteDTO, HttpServletRequest request);
	ResponseEntity<Map<String, Object>> getCalendar(Long id);
	ResponseEntity<List<Map<String, Object>>> getProfileCalendar(String nickname);
	ResponseEntity<List<Map<String, Object>>> getUserCalendar(Users user, HttpServletRequest request);
}
