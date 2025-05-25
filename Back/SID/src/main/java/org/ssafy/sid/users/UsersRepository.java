package org.ssafy.sid.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ssafy.sid.users.model.Users;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
	Boolean existsByEmail(String email);
	Optional<Users> findByEmail(String email);
	Optional<Users> findById(Long id);
}
