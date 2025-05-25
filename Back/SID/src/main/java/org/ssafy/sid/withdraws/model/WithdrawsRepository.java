package org.ssafy.sid.withdraws.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ssafy.sid.users.model.Users;

import java.util.List;
import java.util.Optional;

public interface WithdrawsRepository extends JpaRepository<Withdraws, Long> {
	List<Withdraws> findByRequestedAtBefore(String threshold);
	Optional<Withdraws> findByUser(Users user);
}
