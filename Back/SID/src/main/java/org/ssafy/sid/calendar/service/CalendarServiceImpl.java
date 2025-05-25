package org.ssafy.sid.calendar.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ssafy.sid.calendar.dto.CalendarDeleteDTO;
import org.ssafy.sid.calendar.dto.CalendarSaveDTO;
import org.ssafy.sid.calendar.dto.CalendarUpdateDTO;
import org.ssafy.sid.calendar.model.CalendarEvents;
import org.ssafy.sid.calendar.model.CalendarEventsRepository;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.lastprofiles.model.LastProfilesRepository;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.profiles.model.ProfilesRepository;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.jwt.service.JwtServiceImpl;
import org.ssafy.sid.users.model.Users;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarServiceImpl implements CalendarService {

	private final JwtServiceImpl jwtServiceImpl;
	private final UsersRepository usersRepository;
	private final LastProfilesRepository lastProfilesRepository;
	private final ProfilesRepository profilesRepository;
	private final CalendarEventsRepository calendarEventsRepository;

	@Override
	@Transactional
	public ResponseEntity<Map<String, Object>> createCalendar(CalendarSaveDTO calendarSaveDTO, HttpServletRequest request) {
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

		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());
		if (lastProfiles.isEmpty()) {
			log.error("최근 프로필 기록이 존재하지 않습니다.");
			errorResultMap.put("error", "최근 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		LastProfiles lastProfile = lastProfiles.get(0);
		Profiles profile = lastProfile.getProfile();

		if (!profilesRepository.existsByNickname(profile.getNickname())) {
			log.error("프로필이 존재하지 않습니다.");
			errorResultMap.put("error", "프로필이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		CalendarEvents calendarEvents = calendarEventsRepository.save(calendarSaveDTO.toCalendarEvents(user.get(), profile));

		return ResponseEntity.status(HttpStatus.CREATED).body(getResultMap(calendarEvents, calendarSaveDTO.getAlarmTime(), profile.getNickname()));
	}

	@Override
	@Transactional
	public ResponseEntity<Map<String, Object>> updateCalendar(CalendarUpdateDTO calendarUpdateDTO, HttpServletRequest request) {
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

		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());
		if (lastProfiles.isEmpty()) {
			log.error("최근 프로필 기록이 존재하지 않습니다.");
			errorResultMap.put("error", "최근 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		LastProfiles lastProfile = lastProfiles.get(0);
		Profiles profile = lastProfile.getProfile();

		if (!profilesRepository.existsByNickname(profile.getNickname())) {
			log.error("프로필이 존재하지 않습니다.");
			errorResultMap.put("error", "프로필이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<CalendarEvents> calendarEvents = calendarEventsRepository.findById(calendarUpdateDTO.getScheduleId());
		if (calendarEvents.isEmpty()) {
			log.error("일정이 존재하지 않습니다.");
			errorResultMap.put("error", "일정이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		calendarEvents.get().update(calendarUpdateDTO);

		return ResponseEntity.status(HttpStatus.OK).body(getResultMap(calendarEvents.get(), calendarUpdateDTO.getAlarmTime(), profile.getNickname()));
	}

	@Override
	@Transactional
	public ResponseEntity<Map<String, Object>> deleteCalendar(CalendarDeleteDTO calendarDeleteDTO, HttpServletRequest request) {
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

		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());
		if (lastProfiles.isEmpty()) {
			log.error("최근 프로필 기록이 존재하지 않습니다.");
			errorResultMap.put("error", "최근 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		LastProfiles lastProfile = lastProfiles.get(0);
		Profiles profile = lastProfile.getProfile();

		if (!profilesRepository.existsByNickname(profile.getNickname())) {
			log.error("프로필이 존재하지 않습니다.");
			errorResultMap.put("error", "프로필이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<CalendarEvents> calendarEvents = calendarEventsRepository.findById(calendarDeleteDTO.getScheduleId());
		if (calendarEvents.isEmpty()) {
			log.error("일정이 존재하지 않습니다.");
			errorResultMap.put("error", "일정이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		calendarEventsRepository.delete(calendarEvents.get());
		resultMap.put("message", "delete");

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@Override
	@Transactional
	public ResponseEntity<Map<String, Object>> getCalendar(Long id) {
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		Optional<CalendarEvents> calendarEvents = calendarEventsRepository.findById(id);
		if (calendarEvents.isEmpty()) {
			log.error("일정이 존재하지 않습니다.");
			errorResultMap.put("error", "일정이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		CalendarEvents calendarEvent = calendarEvents.get();

		Map<String, Object> resultMap = new HashMap<String, Object>();

		resultMap.put("scheduleId", id);
		resultMap.put("nickname", calendarEvent.getProfile().getNickname());
		resultMap.put("title", calendarEvent.getTitle());
		resultMap.put("start", calendarEvent.getStartTime());
		resultMap.put("end", calendarEvent.getEndTime());
		resultMap.put("memo", calendarEvent.getMemo());
		resultMap.put("alarmTime", getAlarmTime(calendarEvent));

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@Override
	@Transactional
	public ResponseEntity<List<Map<String, Object>>> getProfileCalendar(String nickname) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		List<Map<String, Object>> errorResultMapList =  new ArrayList<Map<String, Object>>();

		Optional<Profiles> profile = profilesRepository.findByNickname(nickname);
		if (profile.isEmpty()) {
			log.error("프로필이 존재하지 않습니다.");
			errorResultMap.put("error", "프로필이 존재하지 않습니다.");
			errorResultMapList.add(errorResultMap);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMapList);
		}

		List<CalendarEvents> calendarEventsList = calendarEventsRepository.findAllByProfile(profile.get());
		if (calendarEventsList.isEmpty()) {
			log.error("일정이 존재하지 않습니다.");
			errorResultMap.put("error", "일정이 존재하지 않습니다.");
			errorResultMapList.add(errorResultMap);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMapList);
		}

		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		for (CalendarEvents calendarEvent : calendarEventsList) {
			resultList.add(getResultMap(calendarEvent, getAlarmTime(calendarEvent), nickname));
		}

		return ResponseEntity.status(HttpStatus.OK).body(resultList);
	}

	@Override
	@Transactional
	public ResponseEntity<List<Map<String, Object>>> getUserCalendar(Users user, HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		List<Map<String, Object>> errorResultMapList =  new ArrayList<Map<String, Object>>();

		List<CalendarEvents> calendarEventsList = calendarEventsRepository.findAllByUser(user);
		if (calendarEventsList.isEmpty()) {
			log.error("일정이 존재하지 않습니다.");
			errorResultMap.put("error", "일정이 존재하지 않습니다.");
			errorResultMapList.add(errorResultMap);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMapList);
		}

		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		for (CalendarEvents calendarEvent : calendarEventsList) {
			resultList.add(getResultMap(calendarEvent, getAlarmTime(calendarEvent), calendarEvent.getProfile().getNickname()));
		}

		return ResponseEntity.status(HttpStatus.OK).body(resultList);
	}

	private Map<String, Object> getResultMap(CalendarEvents calendarEvents, int alarmTime, String nickname) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		resultMap.put("scheduleId", calendarEvents.getId());
		resultMap.put("nickname", nickname);
		resultMap.put("title", calendarEvents.getTitle());
		resultMap.put("start", calendarEvents.getStartTime());
		resultMap.put("end", calendarEvents.getEndTime());
		resultMap.put("memo", calendarEvents.getMemo());
		resultMap.put("alarmTime", alarmTime);

		return resultMap;
	}

	private int getAlarmTime(CalendarEvents calendarEvents) {
		if (calendarEvents.getAlarmTime() != null) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				// startTime과 alarmTime 모두 같은 형식의 문자열이므로 동일하게 파싱합니다.
				LocalDateTime startDateTime = LocalDateTime.parse(calendarEvents.getStartTime(), formatter);
				LocalDateTime alarmDateTime = LocalDateTime.parse(calendarEvents.getAlarmTime(), formatter);

				// alarmDateTime이 startDateTime보다 이전이라고 가정하면, 두 시간의 차이를 계산합니다.
				Duration duration = Duration.between(alarmDateTime, startDateTime);

				// 시간 단위로 차이를 int로 저장 (예: 1시간 차이면 1, 2시간 차이면 2)
				return  (int) duration.toHours();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return 0;
	}
}

