package org.ssafy.sid.calendar.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.users.model.Users;

import java.util.Calendar;
import java.util.List;

public interface CalendarEventsRepository extends JpaRepository<CalendarEvents, Long> {
	List<CalendarEvents> findAllByProfile(Profiles profile);
	List<CalendarEvents> findAllByUser(Users user);
}
