package org.ssafy.sid.posts.service;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.ssafy.sid.posts.dto.PostsGetDTO;
import org.ssafy.sid.posts.dto.PostsGetMoreDTO;
import org.ssafy.sid.posts.dto.PostsSaveDTO;
import org.ssafy.sid.posts.dto.PostsUpdateDTO;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.users.model.Users;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface PostsService {
	Posts createPost(PostsSaveDTO postsSaveDTO, Profiles profile) throws IOException;
	void updatePost(PostsUpdateDTO postsUpdateDTO, Posts post) throws IOException;
	void deletePost(Posts post) throws IOException;
	Page<PostsGetDTO> getPosts(int page, Profiles profile, int type, List<Long> postIds);
	Page<PostsGetMoreDTO> getMorePosts(int page, Profiles profile, Posts post);
	Page<PostsGetDTO> getProfilePosts (int page, Profiles myProfile, Profiles thisProfile);
	Page<PostsGetDTO> getFollowPosts (int page, Profiles myProfile);
	ResponseEntity<Map<String, Object>> getSearch(String keyword, String type, int page, Profiles myProfile) throws IOException;
	Map<String, Object> getOne(long id, Profiles profile) throws IOException;
	List<PostsGetDTO> getTrendPosts(int page, List<Long> postIds, Profiles profile) throws IOException;
	PostsGetDTO getOthreOne(long id, Profiles profile) throws IOException;
}
