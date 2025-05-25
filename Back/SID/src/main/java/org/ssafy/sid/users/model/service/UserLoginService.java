package org.ssafy.sid.users.model.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.ssafy.sid.users.dto.LoginUserDTO;
import org.ssafy.sid.users.model.Users;

import java.io.IOException;
import java.util.Map;

public interface UserLoginService {
	ResponseEntity<Map<String, Object>> userLogin(LoginUserDTO loginUser, Users user, HttpServletResponse response, Boolean isRemember) throws IOException;
}
