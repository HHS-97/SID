package org.ssafy.sid.users.model.service;

import org.springframework.http.ResponseEntity;
import org.ssafy.sid.users.dto.*;
import org.ssafy.sid.users.model.Users;

import java.util.Map;

public interface UsersService {
	ResponseEntity<Map<String, Object>> create(UsersSaveDTO usersSaveDTO);
	Boolean checkEmailDuplicate(String email);
	LoginUserDTO loginUser(LoginDTO loginDTO, Users user);
	void saveRefreshToken(long user_id, String refresh_token);
	UserDetailDTO userDetail(String email);
	ResponseEntity<Map<String, Object>> updateUser(String email, UserUpdateDTO userUpdateDTO);
	void deleteRefreshToken(String email);
	void updatePassword(String email, String newPassword);
}
