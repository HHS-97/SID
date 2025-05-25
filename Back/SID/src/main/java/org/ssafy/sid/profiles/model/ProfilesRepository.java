package org.ssafy.sid.profiles.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.ssafy.sid.users.model.Users;

import java.util.List;
import java.util.Optional;

public interface ProfilesRepository extends JpaRepository<Profiles, Long> {
	Optional<Profiles> findByNickname(String nickname);
	List<Profiles> findByUser(Users user);
	Boolean existsByNickname(String nickname);
	Boolean existsByUser(Users user);
	Boolean existsByNicknameAndUser(String nickname, Users user);
	Boolean existsByIdAndUser(long id, Users user);
	Page<Profiles> findByNicknameContainingIgnoreCase(String keyword, Pageable pageable);
}
