package org.ssafy.sid.oauth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ssafy.sid.oauth.dto.OAuth2SaveDTO;
import org.ssafy.sid.oauth.service.OAuth2Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/social")
@AllArgsConstructor
public class SocialController {
	
	private final OAuth2Service oAuth2Service;

	@PostMapping("/signup")
	public ResponseEntity<?> SocialSignUp(@RequestParam("email") String email, @RequestParam("gender") String gender,
			@RequestParam("name") String name, @RequestParam("phone") String phone, @RequestParam("birthyear") String birthyear,
			@RequestParam("birthday") String birthday, @RequestParam("provider") String provider){
		
		  String birthYear = birthyear; // 예: "1999"
	      String birthdays = birthday; // 예: "0821"
	
	      String birthMonth = birthdays.substring(0, 2); // "08"
	      String birthDay = birthdays.substring(2); // "21"
	
	      String phoneNumber = phone.replace("+82 ", "0").replace(" ", "").replace("-", "");
	      if (phoneNumber.length() == 11) {
	          phoneNumber = phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3, 7) + "-" + phoneNumber.substring(7);
	      }
	
	      OAuth2SaveDTO oAuth2SaveDTO = OAuth2SaveDTO.builder()
	              .email(email)
	              .name(name)
	              .birthDate(birthYear + "-" + birthMonth + "-" + birthDay)
	              .gender(gender.toUpperCase())
	              .phone(phoneNumber)
	              .provider("kakao")
	              .build();
	
	      oAuth2Service.createUser(oAuth2SaveDTO);
		
		
		return ResponseEntity.ok("Okay");
	}
}
