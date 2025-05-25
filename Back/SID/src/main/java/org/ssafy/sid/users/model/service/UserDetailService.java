package org.ssafy.sid.users.model.service;

import org.ssafy.sid.users.dto.UserDetailDTO;

public interface UserDetailService {

	public UserDetailDTO loadUserByEmail(String email);
}
