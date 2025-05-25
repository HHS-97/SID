package org.ssafy.sid.lastprofiles.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.users.model.Users;

import java.util.List;
import java.util.Optional;

public interface LastProfilesRepository extends JpaRepository<LastProfiles, Long> {
	List<LastProfiles> findByUser(Users user);
	Optional<LastProfiles> findByProfile(Profiles profile);
	Boolean existsByProfile(Profiles profile);
}
