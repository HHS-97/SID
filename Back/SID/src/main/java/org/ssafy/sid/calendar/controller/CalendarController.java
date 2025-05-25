package org.ssafy.sid.calendar.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ssafy.sid.calendar.dto.CalendarDeleteDTO;
import org.ssafy.sid.calendar.dto.CalendarSaveDTO;
import org.ssafy.sid.calendar.dto.CalendarUpdateDTO;
import org.ssafy.sid.calendar.service.CalendarServiceImpl;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.jwt.service.JwtServiceImpl;
import org.ssafy.sid.users.model.Users;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j
public class CalendarController {

	private final CalendarServiceImpl calendarServiceImpl;
	private final JwtServiceImpl jwtServiceImpl;
	private final UsersRepository usersRepository;

	@PostMapping
	public ResponseEntity<?> createCalendar(@RequestBody CalendarSaveDTO calendarSaveDTO, HttpServletRequest request) {
		return calendarServiceImpl.createCalendar(calendarSaveDTO, request);
	}

	@PatchMapping
	public ResponseEntity<?> updateCalendar(@Validated @RequestBody CalendarUpdateDTO calendarUpdateDTO, HttpServletRequest request) {
		return calendarServiceImpl.updateCalendar(calendarUpdateDTO, request);
	}

	@DeleteMapping
	public ResponseEntity<?> deleteCalendar(@Validated @RequestBody CalendarDeleteDTO calendarDeleteDTO, HttpServletRequest request) {
		return calendarServiceImpl.deleteCalendar(calendarDeleteDTO, request);
	}

	@GetMapping("/schedule")
	public ResponseEntity<?> getCalendar(@RequestParam("scheduleId") long scheduleId) {
		return calendarServiceImpl.getCalendar(scheduleId);
	}

	@GetMapping("/profile")
	public ResponseEntity<?> getProfileCalendar(@RequestParam("nickname") String nickname) {
		return calendarServiceImpl.getProfileCalendar(nickname);
	}

	@GetMapping("/user")
	public ResponseEntity<?> getUserCalendar(HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = "";
		if (getEmail.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		} else {
			return getEmail;
		}
		Optional<Users> user = usersRepository.findByEmail(email);

		if (user.isEmpty()) {
			log.error("존재하지 않는 유저입니다.");
			errorResultMap.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		return calendarServiceImpl.getUserCalendar(user.get(), request);
	}
}
