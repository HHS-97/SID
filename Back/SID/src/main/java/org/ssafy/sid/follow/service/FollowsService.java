package org.ssafy.sid.follow.service;

import org.ssafy.sid.follow.dto.FollowsDTO;
import org.ssafy.sid.follow.model.Follows;
import org.ssafy.sid.profiles.model.Profiles;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FollowsService {
	void addFollow(FollowsDTO followsDTO);
	void deleteFollow(Follows follow);
	List<Map<String, Object>> getFollowings(Profiles profile, Profiles lastProfile) throws IOException;
	List<Map<String, Object>> getFollowers(Profiles profile, Profiles lastProfile) throws IOException;
}
