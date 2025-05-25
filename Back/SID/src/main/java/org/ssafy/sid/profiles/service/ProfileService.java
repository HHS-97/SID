package org.ssafy.sid.profiles.service;

import org.ssafy.sid.profiles.dto.ProfileDetailDTO;
import org.ssafy.sid.profiles.dto.ProfileListDTO;
import org.ssafy.sid.profiles.dto.ProfileSaveDTO;
import org.ssafy.sid.profiles.dto.ProfileUpdateDTO;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.users.model.Users;

import java.io.IOException;

public interface ProfileService {
	Profiles createProfile(ProfileSaveDTO profileSaveDTO) throws IOException;
	ProfileDetailDTO profileDetail(Profiles profile);
	String updateProfile(ProfileUpdateDTO profileUpdateDTO, Profiles profile) throws IOException;
	void deleteProfile(String nickname);
	ProfileListDTO profileList(String email) throws IOException;
}
