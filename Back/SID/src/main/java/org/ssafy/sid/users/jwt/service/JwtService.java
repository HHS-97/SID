package org.ssafy.sid.users.jwt.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface JwtService {
	ResponseEntity<?> getEmail(HttpServletRequest request);
	ResponseEntity<Map<String, Object>> getEmailLogout(HttpServletRequest request);
}
