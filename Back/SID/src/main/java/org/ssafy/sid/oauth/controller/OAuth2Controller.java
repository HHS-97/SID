package org.ssafy.sid.oauth.controller;

import org.ssafy.sid.exception.UserNotFoundException;
import org.ssafy.sid.lastprofiles.dto.LastProfileSaveDTO;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.lastprofiles.service.LastProfileServiceImpl;
import org.ssafy.sid.oauth.dto.OAuth2AuthInfoDto;
import org.ssafy.sid.oauth.dto.OAuth2KakaoUserInfoDto;
import org.ssafy.sid.oauth.dto.OAuth2NaverUserInfoDto;
import org.ssafy.sid.oauth.dto.OAuth2SaveDTO;
import org.ssafy.sid.oauth.jpa.OAuth2UserServiceDb;
import org.ssafy.sid.oauth.service.OAuth2Service;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.profiles.model.ProfilesRepository;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.dto.UserDetailDTO;
import org.ssafy.sid.users.dto.UsersSaveDTO;
import org.ssafy.sid.users.jwt.JwtUtil;
import org.ssafy.sid.users.model.Users;
import org.ssafy.sid.users.model.service.UsersService;
import org.ssafy.sid.lastprofiles.model.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import org.ssafy.sid.users.model.service.UsersServiceImpl;

@Slf4j
@RestController
@RequestMapping("/api/login/oauth2/code")
public class OAuth2Controller {
    
    private final OAuth2Service oAuth2Service;
    private final OAuth2UserServiceDb oauth2;
    private final UsersRepository usersRepository;
    private final JwtUtil jwtUtil;
    private final UsersService userService;
    private final LastProfilesRepository lastprofilerepository;
    private final ProfilesRepository profileRepository;
    private final LastProfileServiceImpl lastProfilese;
    private final UsersServiceImpl usersServiceImpl;

    public OAuth2Controller(OAuth2Service oAuth2Service, OAuth2UserServiceDb oauth2, UsersRepository usersRepository, JwtUtil jwtUtil,
                            UsersService userService, LastProfilesRepository lastprofilerepository, ProfilesRepository profileRepository,
                            LastProfileServiceImpl lastProfilese, UsersServiceImpl usersServiceImpl) {
        this.oAuth2Service = oAuth2Service;
        this.oauth2 = oauth2;
        this.usersRepository = usersRepository;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.lastprofilerepository = lastprofilerepository;
        this.profileRepository = profileRepository;
        this.lastProfilese = lastProfilese;
        this.usersServiceImpl = usersServiceImpl;
    }

    @GetMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String error_description,
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response) {
        
        log.debug("Kakao Login - code: {}, error: {}, error_description: {}, state: {}", code, error, error_description, state);
        OAuth2AuthInfoDto kakaoReqDto = OAuth2AuthInfoDto.builder()
                .code(code)
                .error(error)
                .error_description(error_description)
                .state(state)
                .build();
        
//        System.out.println("kakaoreqdto " + kakaoReqDto);
        OAuth2KakaoUserInfoDto resultObj = oAuth2Service.kakaoLogin(kakaoReqDto);
        Boolean ProfileIsEmpty = false;
        
//        System.out.println("완성된 resultObj == " + resultObj);
        
        if (resultObj != null) {
            Optional<Users> existUser = usersRepository.findByEmail(resultObj.getEmail());
            Users user = null;
            
            Map<String, Object> resultMap = new HashMap<>();
            
            if (existUser.isPresent()) {
                // 여기가 user가 있는거
                user = existUser.get();
//                System.out.println(user);
                
                List<LastProfiles> lastProfiles = lastprofilerepository.findByUser(user);
                if (lastProfiles.isEmpty() || lastProfiles == null) {
                    // 없으면 새로생성해야되는거로 가게 나중에 얘기해보기
                    List<Profiles> profilesList = profileRepository.findByUser(user);
                    if (profilesList.isEmpty()) {
                        resultMap.put("lastProfile", null);
                        ProfileIsEmpty = true;
                    } else {
                        lastProfilese.createLastProfiles(LastProfileSaveDTO.builder()
                                .user(user)
                                .profile(profilesList.get(0))
                                .build());
                    }
                } else if (lastProfiles.size() > 1) {
                    // 만약 lastProfile이 여러개 생긴 경우 0번 제외 삭제
                    LastProfiles remainLastProfiles = lastProfiles.get(0);
                    lastProfiles.remove(0);
                    lastprofilerepository.deleteAll(lastProfiles);
                } else {
                    Profiles lastProfile = lastProfiles.get(0).getProfile();
                    lastProfilese.deleteLastProfiles(lastProfiles.get(0));
                    lastProfilese.createLastProfiles(LastProfileSaveDTO.builder()
                            .user(user)
                            .profile(lastProfile)
                            .build());
                }
                
                String accessToken = jwtUtil.createAccessToken(user.getEmail());
                String refreshToken = jwtUtil.createRefreshToken(user.getEmail(), false);
                userService.saveRefreshToken(user.getId(), refreshToken);

                Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
                accessTokenCookie.setHttpOnly(true);
                accessTokenCookie.setPath("/");
                accessTokenCookie.setMaxAge(60 * 60 * 24 * 365); // 1년 유효

                Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
                refreshTokenCookie.setHttpOnly(true);
                refreshTokenCookie.setPath("/");
                refreshTokenCookie.setMaxAge(60 * 60 * 24 * 365); // 1년 유효

                // 쿠키에 토큰 추가
                response.addCookie(accessTokenCookie);
                response.addCookie(refreshTokenCookie);
                resultMap.put("email", user.getEmail()); // 로그인된 사용자 정보 추가
                resultMap.put("message", "login");
                
                if (!ProfileIsEmpty) {
					resultMap.put("lastProfile", lastProfiles.get(0).getProfile().getNickname());
				}
//                System.out.println("여기는 라스트 프로필 " + lastProfiles.get(0).getProfile().getNickname());
                
                

				List<String> profileNicknameList = new ArrayList<>();
				List<Profiles> profilesList = profileRepository.findByUser(user);
				if (!profilesList.isEmpty()) {
					for (Profiles profile : profilesList) {
						profileNicknameList.add(profile.getNickname());
					}
				}
				
				for(Profiles profile : profilesList) {
//					System.out.println("이건 프로필 리스트 : " + profile.getNickname());
				}
				
//				System.out.println(response.getHeader("Set-Cookie"));
				resultMap.put("profileNicknameList", profileNicknameList);

//                System.out.println(response.getHeader("Set-Cookie"));
//                System.out.println(response.getHeader(accessTokenCookie.toString()));
//                System.out.println("access token " + accessToken);
//                System.out.println("refresh token " + refreshToken);
                
                return ResponseEntity.status(HttpStatus.OK).body(resultMap);
            } else {
                //여기가 없는 거
//                String birthYear = resultObj.getBirthyear(); // 예: "1999"
//                String birthday = resultObj.getBirthday(); // 예: "0821"
//
//                String birthMonth = birthday.substring(0, 2); // "08"
//                String birthDay = birthday.substring(2); // "21"
//
//                String phoneNumber = resultObj.getNumber().replace("+82 ", "0").replace(" ", "").replace("-", "");
//                if (phoneNumber.length() == 11) {
//                    phoneNumber = phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3, 7) + "-" + phoneNumber.substring(7);
//                }
//
//                OAuth2SaveDTO oAuth2SaveDTO = OAuth2SaveDTO.builder()
//                        .email(resultObj.getEmail())
//                        .name(resultObj.getName())
//                        .birthDate(birthYear + "-" + birthMonth + "-" + birthDay)
//                        .gender(resultObj.getGender())
//                        .phone(phoneNumber)
//                        .provider("kakao")
//                        .build();
//
//                oAuth2Service.createUser(oAuth2SaveDTO);
//
//                resultMap.put("email", resultObj.getEmail());
//                resultMap.put("message", "create");
//
//                return ResponseEntity.status(HttpStatus.CREATED).body(resultMap);
            	  Map<String, Object> signMap = new HashMap<>();
            	  signMap.put("email", resultObj.getEmail());
            	  signMap.put("name", resultObj.getName());
            	  signMap.put("birthYear", resultObj.getBirthyear());
            	  signMap.put("birthday", resultObj.getBirthday());
            	  signMap.put("gender", resultObj.getGender());
            	  signMap.put("phone", resultObj.getNumber().replace("+82 ", "0").replace(" ", "").replace("-", ""));
            	  signMap.put("message", "terms_required");
            	  signMap.put("provider", "kakao");
                  
                  return ResponseEntity.status(HttpStatus.OK).body(signMap);
            }
        }

        return new ResponseEntity<>(resultObj, HttpStatus.OK);
    }
    
    @GetMapping("/naver")
    public ResponseEntity<?> NaverLogin(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String error_description,
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response) {
        
        log.debug("Naver Login - code: {}, error: {}, error_description: {}, state: {}", code, error, error_description, state);
        OAuth2AuthInfoDto naverReqDto = OAuth2AuthInfoDto.builder()
                .code(code)
                .error(error)
                .error_description(error_description)
                .state(state)
                .build();
        
//        System.out.println("여긴 컨트롤러임 naverReqDto " + naverReqDto);
        OAuth2NaverUserInfoDto resultObj = oAuth2Service.naverLogin(naverReqDto);
        Boolean ProfileIsEmpty = false;
        
//        System.out.println("완성된 resultObj == " + resultObj);
        
        if (resultObj != null) {
            Optional<Users> existUser = usersRepository.findByEmail(resultObj.getResponse().getEmail());
            Users user = null;
            
            Map<String, Object> resultMap = new HashMap<>();
            
            if (existUser.isPresent()) {
                user = existUser.get();
//                System.out.println(user);
                
                List<LastProfiles> lastProfiles = lastprofilerepository.findByUser(user);
                if (lastProfiles.isEmpty()) {
                    List<Profiles> profilesList = profileRepository.findByUser(user);
                    if (profilesList.isEmpty()) {
                        resultMap.put("lastProfile", null);
                        ProfileIsEmpty = true;
                    } else {
                        lastProfilese.createLastProfiles(LastProfileSaveDTO.builder()
                                .user(user)
                                .profile(profilesList.get(0))
                                .build());
                    }
                } else if (lastProfiles.size() > 1) {
                    LastProfiles remainLastProfiles = lastProfiles.get(0);
                    lastProfiles.remove(0);
                    lastprofilerepository.deleteAll(lastProfiles);
                } else {
                    Profiles lastProfile = lastProfiles.get(0).getProfile();
                    lastProfilese.deleteLastProfiles(lastProfiles.get(0));
                    lastProfilese.createLastProfiles(LastProfileSaveDTO.builder()
                            .user(user)
                            .profile(lastProfile)
                            .build());
                }
                
                String accessToken = jwtUtil.createAccessToken(user.getEmail());
                String refreshToken = jwtUtil.createRefreshToken(user.getEmail(), false);
                userService.saveRefreshToken(user.getId(), refreshToken);

                Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
                accessTokenCookie.setHttpOnly(true);
                accessTokenCookie.setPath("/");
                accessTokenCookie.setMaxAge(60 * 60 * 24 * 365); // 1년 유효

                Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
                refreshTokenCookie.setHttpOnly(true);
                refreshTokenCookie.setPath("/");
                refreshTokenCookie.setMaxAge(60 * 60 * 24 * 365); // 1년 유효

                response.addCookie(accessTokenCookie);
                response.addCookie(refreshTokenCookie);
                resultMap.put("email", user.getEmail());
                resultMap.put("message", "login");
                
                if (!ProfileIsEmpty) {
					resultMap.put("lastProfile", lastProfiles.get(0).getProfile().getNickname());
				}
//                System.out.println("여기는 라스트 프로필 " + lastProfiles.get(0).getProfile().getNickname());
                
                

				List<String> profileNicknameList = new ArrayList<>();
				List<Profiles> profilesList = profileRepository.findByUser(user);
				if (!profilesList.isEmpty()) {
					for (Profiles profile : profilesList) {
						profileNicknameList.add(profile.getNickname());
					}
				}
				
				for(Profiles profile : profilesList) {
					log.debug("이건 프로필 리스트 : " + profile.getNickname());
				}
				
//				System.out.println(response.getHeader("Set-Cookie"));
				resultMap.put("profileNicknameList", profileNicknameList);
//				System.out.println(response.getHeader("Set-Cookie"));
				resultMap.put("profileNicknameList", profileNicknameList);
                

//                System.out.println(response.getHeader("Set-Cookie"));
//                System.out.println(response.getHeader(accessTokenCookie.toString()));
//                System.out.println("access token " + accessToken);
//                System.out.println("refresh token " + refreshToken);
                return ResponseEntity.status(HttpStatus.OK).body(resultMap);
            } else {
               
            	String birthYear = resultObj.getResponse().getBirthyear(); // 예: "1999"
				String birthday = resultObj.getResponse().getBirthday(); // 예: "08-21"
				
//                OAuth2SaveDTO oAuth2SaveDTO = OAuth2SaveDTO.builder()
//                        .email(resultObj.getResponse().getEmail())
//                        .name(resultObj.getResponse().getName())
//                        .birthDate(birthYear + "-" + birthday)
//                        .gender(resultObj.getResponse().getGender())
//                        .phone(resultObj.getResponse().getMobile())
//                        .provider("naver")
//                        .build();
//
//                oAuth2Service.createUser(oAuth2SaveDTO);
//                resultMap.put("email", resultObj.getResponse().getEmail());
//                resultMap.put("message", "create");
				  Map<String, Object> signMap = new HashMap<>();
            	  signMap.put("email", resultObj.getResponse().getEmail());
            	  signMap.put("name", resultObj.getResponse().getName());
            	  signMap.put("birthYear", resultObj.getResponse().getBirthyear());
            	  signMap.put("birthday", resultObj.getResponse().getBirthday());
            	  signMap.put("gender", resultObj.getResponse().getGender());
            	  signMap.put("phone", resultObj.getResponse().getMobile());
            	  signMap.put("message", "terms_required");
            	  signMap.put("provider", "naver");
                  
                  return ResponseEntity.status(HttpStatus.OK).body(signMap);
            }
        }
        return new ResponseEntity<>(resultObj, HttpStatus.OK);
    }
}
