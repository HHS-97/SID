package org.ssafy.sid.users.jwt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ssafy.sid.users.model.Users;

import java.util.List;

public interface RefreshTokensRepository extends JpaRepository<RefreshTokens, Long> {
	boolean existsByRefreshToken(String refreshToken);
	void deleteByUser(Users user);
	String findByUser(Users user);
	List<RefreshTokens> findAllByUser(Users user);
}
