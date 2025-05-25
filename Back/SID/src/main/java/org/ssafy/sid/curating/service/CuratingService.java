package org.ssafy.sid.curating.service;

import org.ssafy.sid.posts.dto.PostsGetDTO;
import org.ssafy.sid.profiles.model.Profiles;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CuratingService {
	List<PostsGetDTO> getCuratingPosts(int page, String uri, List<Long> postIds, Profiles profile) throws IOException;
}
