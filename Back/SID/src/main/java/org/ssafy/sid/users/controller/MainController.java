package org.ssafy.sid.users.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ssafy.sid.util.CookieUtils;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class MainController {

	@GetMapping("/api")
	public String home() {
		return "forward:/index.html";
	}

	@DeleteMapping("/api/cookie")
	@ResponseBody
	public ResponseEntity<?> cookie(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> resultMap = new HashMap<>();

		CookieUtils.deleteCookie(request, response, "accessToken");
		CookieUtils.deleteCookie(request, response, "refreshToken");

		resultMap.put("message", "True");
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}
}
