package org.ssafy.sid.users.model.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ssafy.sid.exception.UserNotFoundException;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.dto.UserDetailDTO;
import org.ssafy.sid.users.model.Users;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailServiceImpl implements UserDetailService {

	private final UsersRepository usersRepository;

	@Override
	@Transactional
	public UserDetailDTO loadUserByEmail(String email) {
		Users user = usersRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("등록되지 않은 회원입니다."));

		UserDetailDTO userDetail = UserDetailDTO.builder()
				.email(user.getEmail())
				.name(user.getName())
				.provider(user.getProvider())
				.phone(user.getPhone())
				.role(user.getRole())
				.status(user.getStatus())
				.gender(user.getGender())
				.birthDate(user.getBirthDate())
				.penalty(user.getPenalty())
				.build();

		return userDetail;
	}
}
