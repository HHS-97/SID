package org.ssafy.sid.oauth.jpa;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ssafy.sid.oauth.dto.OAuth2KakaoUserInfoDto;


@Service
public class OAuth2UserServiceDb {

	@Autowired
	private OAuth2UserRepository oauth2UserRepository;
	
	public void saveUser(OAuth2KakaoUserInfoDto resultObj) {
//		System.out.println("save");
		OAuth2User user = new OAuth2User();
		   
		String birthYear = resultObj.getBirthyear(); // 예: "1999"
		String birthday = resultObj.getBirthday(); // 예: "0821"

		String birthMonth = birthday.substring(0, 2); // "08"
		String birthDay = birthday.substring(2); // "21"

		String birthDate = birthYear + "-" + birthMonth + "-" + birthDay;
		   
		user.setBirthDate(birthDate); // 형식에 맞게 조합
        user.setCreatedAt(LocalDateTime.now().plusHours(9));
        user.setEmail(resultObj.getEmail());
        
        
        String gender = resultObj.getGender(); // 예: "male" 또는 "female"
        String genderCode;

        if ("male".equalsIgnoreCase(gender)) {
            genderCode = "M";
        } else if ("female".equalsIgnoreCase(gender)) {
            genderCode = "F";
        } else {
            genderCode = "U"; // 미정의된 경우, 예를 들어 "U"를 사용
        }

        user.setGender(genderCode);
        user.setName(resultObj.getName());
        user.setPassword(null); // password를 null로 설정
        user.setPenalty(null); // 필요에 따라 설정
        
        String phoneNumber = resultObj.getNumber().replace("+82 ", "0").replace(" ", "").replace("-", "");
        if (phoneNumber.length() == 11) {
            phoneNumber = phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3, 7) + "-" + phoneNumber.substring(7);
        }
        user.setPhone(phoneNumber); // 형식에 맞게 조합
        
        
        user.setProvider(resultObj.getProvider());
        user.setRole("N"); // 기본 역할 설정
        user.setStatus("N"); // 기본 상태 설정
        user.setUpdatedAt(LocalDateTime.now().plusHours(9));

        oauth2UserRepository.save(user); // 데이터베이스에 저장
	}
	
}
