package org.ssafy.sid.oauth.service;

import java.util.Map;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.ssafy.sid.config.OAuth2ProviderProperties;
import org.ssafy.sid.config.OAuth2RegistartionProperties;
import org.ssafy.sid.config.OAuthConfig;
import org.ssafy.sid.config.RestTemplateConfig;
import org.springframework.web.client.RestTemplate;
import org.ssafy.sid.exception.InvalidPasswordException;
import org.ssafy.sid.oauth.dto.OAuth2AuthInfoDto;
import org.ssafy.sid.oauth.dto.OAuth2KakaoUserInfoDto;
import org.ssafy.sid.oauth.dto.OAuth2NaverUserInfoDto;
import org.ssafy.sid.oauth.dto.OAuth2SaveDTO;
import org.ssafy.sid.oauth.dto.OAuth2TokenInfoDto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.dto.UsersSaveDTO;
import org.ssafy.sid.users.model.Users;

@Slf4j
@Service("OAuth2ServiceImpl")
public class OAuth2ServiceImpl implements OAuth2Service{
	
	@Autowired
	private OAuthConfig oauthconfig;
	
	@Autowired
	private final RestTemplateConfig restTemplateConfig;
	private final OAuth2ProviderProperties oAuthProvider;
	private final OAuth2RegistartionProperties oAuthRegistration;
	@Autowired
	private UsersRepository usersRepository;


	public OAuth2ServiceImpl(RestTemplateConfig restTemplateConfig, OAuth2ProviderProperties oAuthProvider, OAuth2RegistartionProperties oAuthRegistration) {
		this.restTemplateConfig = restTemplateConfig;
		this.oAuthProvider = oAuthProvider;
		this.oAuthRegistration = oAuthRegistration;
	}

	
	private HttpHeaders defaultHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
//		headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
		return headers;
	}
	 
	@Override
	public OAuth2KakaoUserInfoDto kakaoLogin(OAuth2AuthInfoDto authInfo) {
		
		 log.debug("[+] 카카오 로그인이 성공하여 리다이렉트 되었습니다.", authInfo);
	     log.debug("코드 값 확인 : {}", authInfo.getCode());
	     log.debug("에러 값 확인 : {}", authInfo.getError());
	     log.debug("에러 설명 값 확인 : {}", authInfo.getError_description());
	     log.debug("상태 값 확인 : {}", authInfo.getState());

		if(authInfo.getCode() == null || authInfo.getCode().isEmpty()) {
			log.error("[-] 카카오 로그인 리다이렉션에서 문제가 발생하였습니다.");
	        return null;
		}
		

        // [STEP2] 카카오로 토큰을 요청합니다.(접근 토큰, 갱신 토큰)
        OAuth2TokenInfoDto kakaoTokenInfo = this.getKakaoTokenInfo(authInfo.getCode());
        log.debug("토큰 정보 전체를 확인합니다 :: {}", kakaoTokenInfo);

        // [STEP3] 접근 토큰을 기반으로 사용자 정보를 요청합니다.
        OAuth2KakaoUserInfoDto userInfo = this.getKakaoUserInfo(kakaoTokenInfo.getAccessToken());
        log.debug("userInfo :: {}", userInfo);
        return userInfo;
		
	}
	
	private Map<String, Object> cvtObjectToMap(Object obj) {
	    ObjectMapper mapper = new ObjectMapper();
	    return mapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
	}
	


	private OAuth2TokenInfoDto getKakaoTokenInfo(String authcode) {
		
		log.debug("[+] getKakaoTokenInfo 함수가 실행 됩니다. :: {}", authcode);
		OAuth2TokenInfoDto resultDto = null;
		ResponseEntity<Map<String, Object>> responseTokenInfo = null;

		// [STEP1] 카카오 토큰 URL로 전송할 데이터 구성
//		System.out.println("clientid ==  " + oauthconfig.getClientId());
//		System.out.println("redirect_uri == " + oauthconfig.getRedirectUri());
//		System.out.println("code == " + authcode);
//		System.out.println("serect == " + oauthconfig.getClientSecret());
		
		MultiValueMap<String, Object> requestParamMap = new LinkedMultiValueMap<>();
        requestParamMap.add("grant_type", "authorization_code");
        requestParamMap.add("client_id", oauthconfig.getClientId());
        requestParamMap.add("redirect_uri", oauthconfig.getRedirectUri());
        requestParamMap.add("code", authcode);
        requestParamMap.add("client_secret", oauthconfig.getClientSecret());
        HttpEntity<MultiValueMap<String, Object>> requestMap = new HttpEntity<>(requestParamMap, this.defaultHeader());
        
//        System.out.println(requestMap);
//        System.out.println("gettoken uri == " + oAuthProvider.getKakao().getTokenUri());
		
        try {
            // [STEP2] 카카오 토큰 URL로 RestTemplate 이용하여 데이터 전송
//        	System.out.println("들어오긴 함");
        	responseTokenInfo = restTemplateConfig
        	        .restTemplate()
        	        .exchange(oAuthProvider.getKakao().getTokenUri(), HttpMethod.POST, requestMap, new ParameterizedTypeReference<Map<String, Object>>() {});
//        	System.out.println(responseTokenInfo);
        } catch (Exception e) {
            log.error("[-] 토큰 요청 중에 오류가 발생하였습니다. {}", e.	getMessage());
        }
        // [STEP3] 토큰 반환 값 결과값으로 구성
        if (responseTokenInfo != null && responseTokenInfo.getBody() != null && responseTokenInfo.getStatusCode().is2xxSuccessful()) {
        	Map<String, Object> body = responseTokenInfo.getBody();
            if (body != null) {
                resultDto = OAuth2TokenInfoDto.builder()
                        .accessToken(body.get("access_token").toString())
                        .refreshToken(body.get("refresh_token").toString())
                        .tokenType(body.get("token_type").toString())
                        .build();
            }
        } else {
            log.error("[-] 토큰 정보가 존재하지 않습니다.");
        }
//        System.out.println(resultDto);
        return resultDto;
	}
	
	
	 private OAuth2KakaoUserInfoDto getKakaoUserInfo(String accessToken) {
	        log.debug("[+] getKakaoUserInfo을 수행합니다 :: {}", accessToken);

	        ResponseEntity<Map<String, Object>> responseUserInfo = null;
	        OAuth2KakaoUserInfoDto resultDto = null;

//	        System.out.println("여기는 유저인포 디티오 == " + accessToken);
	        
	        // [STEP1] 필수 요청 Header 값 구성 : ContentType, Authorization
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
	        headers.add("Authorization", "Bearer " + accessToken);        // accessToken 추가

	        // [STEP2] 요청 파라미터 구성 : 원하는 사용자 정보
	        MultiValueMap<String, Object> userInfoParam = new LinkedMultiValueMap<>();
	        ObjectMapper objectMapper = new ObjectMapper();
	        try {
//	        	System.out.println("이거는 등록 카카오 스코프 == " + oAuthRegistration.getKakao().getScope());
	            userInfoParam.add("property_keys", objectMapper.writeValueAsString(oAuthRegistration.getKakao().getScope()));   // 불러올 데이터 조회 (리스트 to 문자열 변환)
	        } catch (JsonProcessingException e) {
	            throw new RuntimeException(e);
	        }
	        HttpEntity<MultiValueMap<String, Object>> userInfoReq = new HttpEntity<>(userInfoParam, headers);

	        // [STEP3] 요청 Header, 파라미터를 포함하여 사용자 정보 조회 URL로 요청을 수행합니다.
	        try {
//	        	System.out.println("여기는 스텝3");
//	        	System.out.println("userinforeq == " + userInfoReq);
	            responseUserInfo = restTemplateConfig
	                    .restTemplate()
	                    .exchange(oAuthProvider.getKakao().getUserInfoUri(), HttpMethod.POST, userInfoReq, new ParameterizedTypeReference<>() {
	                    });
	            log.debug("결과 값 :: {}", responseUserInfo);
//	            System.out.println("this is responsedto" + responseUserInfo);

	        } catch (Exception e) {
	            log.error("[-] 사용자 정보 요청 중에 오류가 발생하였습니다.{}", e.getMessage());
	        }

	        // [STEP4] 사용자 정보가 존재한다면 값을 불러와서 OAuth2KakaoUserInfoDto 객체로 구성하여 반환합니다.
//	        System.out.println("step4");
	        if (responseUserInfo != null && responseUserInfo.getBody() != null && responseUserInfo.getStatusCode().is2xxSuccessful()) {
	        	Map<String, Object> body = responseUserInfo.getBody();
//	        	System.out.println(body);
	            if (body != null) {
	            	Map<String, Object> kakaoAccount = this.cvtObjectToMap(body.get("kakao_account"));
	            	Map<String, Object> profile = this.cvtObjectToMap(this.cvtObjectToMap(body.get("kakao_account")).get("profile"));
//	            	System.out.println("kakaoAccount == " + kakaoAccount);
//	            	System.out.println("profile == " + profile );
//	            	System.out.println("이거 테스트용 == " + kakaoAccount.get("email"));
//	            	System.out.println("이거 테스트용 2 == " + kakaoAccount.get("name").toString());
//	            	System.out.println("이거 프로필 테스트 == " + profile.get("nickname"));
//	            	System.out.println(kakaoAccount.get("name"));
//	            	System.out.println(kakaoAccount.get("age_range"));
//	            	System.out.println(kakaoAccount.get("birthday"));
//	            	System.out.println(kakaoAccount.get("birthyear"));
//	            	System.err.println(kakaoAccount.get("phone_number"));
	            	
	                resultDto = OAuth2KakaoUserInfoDto.builder()
	                        .id(body.get("id").toString())                                      // 사용자 아이디 번호
	                        .statusCode(responseUserInfo.getStatusCode().value())               // 상태 코드
	                        .email(kakaoAccount.get("email").toString())                        // 이메일
	               
	                        .name(kakaoAccount.get("name").toString())
	                        .gender(kakaoAccount.get("gender").toString())
//	                        .age_range(kakaoAccount.get("age_range").toString())
	                        .birthday(kakaoAccount.get("birthday").toString())
	                        .birthyear(kakaoAccount.get("birthyear").toString())
	                        .number(kakaoAccount.get("phone_number").toString())
	                   
	                        .nickname(profile.get("nickname").toString())
	                        .provider("kakao")
	                        .build();
	                
	                if(resultDto == null || resultDto.equals("")) {
	                	log.debug("resultDto는 null");
	                }
	                log.debug("step4 resultdto == " + resultDto);
	                log.debug("최종 구성 결과 :: {}", resultDto);

	            }
	        }
	        return resultDto;
	    }
	 
	 
	@Override
	public OAuth2NaverUserInfoDto naverLogin(OAuth2AuthInfoDto oAuth2AuthInfoDto) {
		
		OAuth2NaverUserInfoDto resultUserInfo = null;

		log.debug("[+] 네이버 로그인이 성공하여 리다이렉트 되었습니다.");
        log.debug("코드 값 확인2 : {}", oAuth2AuthInfoDto.getCode());
        log.debug("에러 값 확인2 : {}", oAuth2AuthInfoDto.getError());
        log.debug("에러 설명 값 확인2 : {}", oAuth2AuthInfoDto.getError_description());
        log.debug("상태 값 확인2 : {}", oAuth2AuthInfoDto.getState());

        if (oAuth2AuthInfoDto.getCode() == null || oAuth2AuthInfoDto.getCode().isEmpty()) {
            log.error("[-] 카카오 로그인 리다이렉션에서 문제가 발생하였습니다.");
            return null;
        }
        
		
        // [STEP2] 전달받은 인증코드를 기반으로 토큰정보를 조회합니다.
        OAuth2TokenInfoDto naverTokenInfo = this.getNaverTokenInfo(oAuth2AuthInfoDto.getCode(), oAuth2AuthInfoDto.getState());

        // [STEP3] 토큰 정보가 존재하는 경우 사용자 정보를 조회합니다.
        // [STEP3] 접근 토큰을 조회합니다.
        String accessToken = naverTokenInfo.getAccessToken();
        String refreshToken = naverTokenInfo.getRefreshToken();
        log.debug("naverTokenInfo :: {} ,  {}", accessToken, refreshToken);
//        System.out.println("naver acceess" + naverTokenInfo.getAccessToken());
//        System.out.println("naver refresh " + naverTokenInfo.getRefreshToken());

        resultUserInfo = this.getNaverUserInfo(accessToken);

        return resultUserInfo;
	}
	
	private OAuth2TokenInfoDto getNaverTokenInfo(String authCode, String state) {
        log.debug("[+] getNaverTokenInfo 함수가 실행 됩니다. :: {}", authCode);
        OAuth2TokenInfoDto resultDto = null;
        ResponseEntity<Map<String, Object>> responseTokenInfo = null;

        // [STEP1] 네이버 토큰 URL로 전송할 데이터 구성
//        System.out.println("네이버 토큰 url 전송 데이터 구성 = " + oAuthRegistration.getNaver().getClientId());
        MultiValueMap<String, String> requestParamMap = new LinkedMultiValueMap<>();
        requestParamMap.add("grant_type", "authorization_code");                          // 인증 과정에 대한 구분값: 1. 발급:'authorization_code', 2. 갱신:'refresh_token', 3. 삭제: 'delete'
        requestParamMap.add("client_id", oAuthRegistration.getNaver().getClientId());           // 애플리케이션 등록 시 발급받은 Client ID 값
        requestParamMap.add("client_secret", oAuthRegistration.getNaver().getClientSecret());   // 애플리케이션 등록 시 발급받은 Client secret 값
        requestParamMap.add("code", authCode);                                            // 로그인 인증 요청 API 호출에 성공하고 리턴받은 인증코드값 (authorization code)
        requestParamMap.add("state", state);                                              // 사이트 간 요청 위조(cross-site request forgery) 공격을 방지하기 위해 애플리케이션에서 생성한 상태 토큰값으로 URL 인코딩을 적용한 값을 사용
        requestParamMap.add("redirect_uri", oAuthRegistration.getNaver().getRedirectUri());     // 애플리케이션 등록 시 발급받은 Client secret 값
        HttpEntity<MultiValueMap<String, String>> requestMap = new HttpEntity<>(requestParamMap, this.defaultHeader());

//        System.out.println("naver request map" + requestMap);
        try {
            // [STEP2] 네이버 토큰 URL로 RestTemplate 이용하여 데이터 전송
            responseTokenInfo = restTemplateConfig
                    .restTemplate()
                    .exchange(oAuthProvider.getNaver().getTokenUri(), HttpMethod.POST, requestMap, new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            log.debug("네이버 로그인 결과 :: {}", responseTokenInfo);
        } catch (Exception e) {
            log.error("[-] 토큰 요청 중에 오류가 발생하였습니다.{}", e.getMessage());
        }

        // [STEP3] 토큰 반환 값 결과값으로 구성
        if (responseTokenInfo != null && responseTokenInfo.getBody() != null && responseTokenInfo.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> body = responseTokenInfo.getBody();
            if (body != null) {
                resultDto = OAuth2TokenInfoDto.builder()
                        .accessToken(body.get("access_token").toString())
                        .refreshToken(body.get("refresh_token").toString())
                        .tokenType(body.get("token_type").toString())
                        .expiresIn(body.get("expires_in").toString())
                        .build();
            }

        } else {
            log.error("[-] 토큰 정보가 존재하지 않습니다.");
        }
        log.debug("최종 결과 값을 확인합니다 : {}", resultDto.toString());

        return resultDto;
    }

    /**
     * Naver의 사용자 정보를 조회합니다.
     *
     * @param accessToken
     * @return
     * @refrence <https://developers.naver.com/docs/login/profile/profile.md>
     */
    private OAuth2NaverUserInfoDto getNaverUserInfo(String accessToken) {
        log.debug("[+] getNaverUserInfo 함수를 수행합니다 :: {}", accessToken);

//        System.out.println("naver user info 까진느 들어옴");
        ResponseEntity<Map<String, Object>> responseUserInfo = null;
        OAuth2NaverUserInfoDto resultDto = null;

        // [STEP1] 필수 요청 Header 값 구성 : ContentType, Authorization
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "Bearer " + accessToken);        // accessToekn 추가

        // [STEP2] 요청 파라미터 구성 : 별도의 요청정보는 없음.
        MultiValueMap<String, Object> userInfoParam = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, Object>> userInfoReq = new HttpEntity<>(userInfoParam, headers);
        log.debug("요청 값 :: {}", userInfoReq);

        // [STEP3] 요청 Header, 파라미터를 포함하여 사용자 정보 조회 URL로 요청을 수행합니다.
        try {
            responseUserInfo = restTemplateConfig
                    .restTemplate()
                    .exchange(oAuthProvider.getNaver().getUserInfoUri(), HttpMethod.POST, userInfoReq,   new ParameterizedTypeReference<Map<String, Object>>() {
                    });

        } catch (Exception e) {
            log.error("[-] 사용자 정보 요청 중에 오류가 발생하였습니다.{}", e.getMessage());
        }

        log.debug("사용자 조회 :: {}", responseUserInfo);
//        System.out.println("naver responseuserinfo" + responseUserInfo);

        // [STEP4] 사용자 정보가 존재한다면 값을 불러와서 OAuth2NaverUserInfoDto 객체로 구성하여 반환합니다.
        if (responseUserInfo != null && responseUserInfo.getBody() != null && responseUserInfo.getStatusCode().is2xxSuccessful()) {
        	Map<String, Object> body = responseUserInfo.getBody();
        	
            if (body != null && body.get("response") != null) {
            	Map<String, Object> resBody = this.cvtObjectToMap(body.get("response"));
//            	System.out.println("naver id = " + resBody.get("id").toString());
//            	System.out.println("naver email = " + resBody.get("email").toString());
//            	System.out.println("naver name = " + resBody.get("name").toString());
//            	System.out.println("naver gender = " + resBody.get("gender").toString());
//            	System.out.println("naver birthday = " + resBody.get("birthday").toString());
//            	System.out.println("naver birthyear = " + resBody.get("birthyear").toString());
//            	System.out.println("naver mobile = " + resBody.get("mobile").toString());
            	
            	
                resultDto = OAuth2NaverUserInfoDto.builder()
                        .resultcode(body.get("resultcode").toString())
                        .message(body.get("message").toString())
                        .response(
                                OAuth2NaverUserInfoDto.NaverUserResponse
                                        .builder()
                                        .id(resBody.get("id").toString())
                                        .email(resBody.get("email").toString())
                                        .name(resBody.get("name").toString())
                                        .gender(resBody.get("gender").toString())
                                        .birthday(resBody.get("birthday").toString())
                                        .birthyear(resBody.get("birthyear").toString())
                                        .mobile(resBody.get("mobile").toString())
                                        .build())
                        .build();
                log.debug("userInfo :: {}", resultDto);
            }
        }
        return resultDto;
    }
	
	 
	
	@Override
	@Transactional
	public Users createUser(OAuth2SaveDTO oAuthSaveDTO) {
		// save
		Users user = Users.builder()
				.email(oAuthSaveDTO.getEmail())
				.name(oAuthSaveDTO.getName())
				.gender(oAuthSaveDTO.getGender().charAt(0))
				.birthDate(oAuthSaveDTO.getBirthDate())
				.phone(oAuthSaveDTO.getPhone())
				.provider(oAuthSaveDTO.getProvider())
				.build();

		return usersRepository.save(user);
	}
}
