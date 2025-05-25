package org.ssafy.sid.withdraws.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ssafy.sid.response.ErrorResponse;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.jwt.JwtUtil;
import org.ssafy.sid.users.model.Users;
import org.ssafy.sid.withdraws.dto.WithdrawDeleteDTO;
import org.ssafy.sid.withdraws.dto.WithdrawRequestDTO;
import org.ssafy.sid.withdraws.dto.WithdrawSaveDTO;
import org.ssafy.sid.withdraws.model.Withdraws;
import org.ssafy.sid.withdraws.model.WithdrawsRepository;
import org.ssafy.sid.withdraws.service.WithdrawsServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/withdraw")
@Slf4j
public class WithdrawController {

	private final JwtUtil jwtUtil;
	private final UsersRepository usersRepository;
	private final WithdrawsServiceImpl withdrawsServiceImpl;
	private final WithdrawsRepository withdrawsRepository;

	// 회원 탈퇴 기능
	@PostMapping
	public ResponseEntity<?> withdrawSub(@RequestBody WithdrawRequestDTO withdrawRequestDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();

		String accessToken = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("accessToken".equals(cookie.getName())) {
					accessToken = cookie.getValue();
					break;
				}
			}
		}

		// accessToken이 없는 경우 처리
		if (accessToken == null) {
			log.error("Access token이 없습니다");
			errorResultMap.put("error", "Access token이 없습니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResultMap);
		}

		String email = jwtUtil.extractEmail(accessToken);
		Optional<Users> users = usersRepository.findByEmail(email);
		if (users.isEmpty()) {
			log.error("계정이 없습니다");
			errorResultMap.put("error", "계정이 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Users user = users.get();
		WithdrawSaveDTO withdrawSaveDTO = WithdrawSaveDTO.builder().reason(withdrawRequestDTO.getReason()).user(user).build();

		withdrawsServiceImpl.withdrawSub(withdrawSaveDTO);

		resultMap.put("message", "successful");

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	// 회원탈퇴 취소
	@DeleteMapping
	public ResponseEntity<?> withdrawCancel(@RequestBody WithdrawDeleteDTO withdrawDeleteDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();

		Optional<Users> users = usersRepository.findByEmail(withdrawDeleteDTO.getEmail());
		if (users.isEmpty()) {
			log.error("계정이 없습니다");
			errorResultMap.put("error", "계정이 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Users user = users.get();
		Optional<Withdraws> withdraws = withdrawsRepository.findByUser(user);
		if (withdraws.isEmpty()) {
			log.error("회원탈퇴 신청 내역이 없습니다");
			errorResultMap.put("error", "회원탈퇴 신청 내역이 없습니다");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		withdrawsServiceImpl.withdrawCancel(withdraws.get());

		resultMap.put("message", "successful");

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}
}
