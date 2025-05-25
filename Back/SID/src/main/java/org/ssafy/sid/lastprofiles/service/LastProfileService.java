package org.ssafy.sid.lastprofiles.service;

import org.ssafy.sid.lastprofiles.dto.LastProfileSaveDTO;
import org.ssafy.sid.lastprofiles.dto.LastProfileUpdateDTO;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.users.model.Users;

public interface LastProfileService {
	void createLastProfiles(LastProfileSaveDTO createLastProfileDTO);
	String updateLastProfiles(LastProfileUpdateDTO lastProfileUpdateDTO, Users user);
	void deleteLastProfiles(LastProfiles lastProfiles);
	Boolean getLastProfile(Users user);
}
