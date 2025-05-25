package org.ssafy.sid.comments.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ssafy.sid.comments.dto.CommentsDeleteDTO;
import org.ssafy.sid.comments.dto.CommentsSaveDTO;
import org.ssafy.sid.comments.dto.CommentsUpdateDTO;
import org.ssafy.sid.comments.model.Comments;
import org.ssafy.sid.comments.model.CommentsRepository;
import org.ssafy.sid.comments.service.CommentsServiceImpl;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.lastprofiles.model.LastProfilesRepository;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.posts.model.PostsRepository;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.jwt.JwtUtil;
import org.ssafy.sid.users.jwt.service.JwtServiceImpl;
import org.ssafy.sid.users.model.Users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentsController {

	private final JwtUtil jwtUtil;
	private final UsersRepository usersRepository;
	private final LastProfilesRepository lastProfilesRepository;
	private final PostsRepository postsRepository;
	private final CommentsServiceImpl commentsServiceImpl;
	private final CommentsRepository commentsRepository;
	private final JwtServiceImpl jwtServiceImpl;

	@PostMapping
	public ResponseEntity<?> createComment(@RequestBody CommentsSaveDTO commentsSaveDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		Posts post;
		Profiles profile;

		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = "";
		if (getEmail.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		} else {
			return getEmail;
		}
		Optional<Users> user = usersRepository.findByEmail(email);
		if (user.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());
		if (lastProfiles.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<Posts> posts = postsRepository.findById(commentsSaveDTO.getPostId());
		if (posts.isPresent()) {
			post = posts.get();
			profile = lastProfiles.get(0).getProfile();
		} else {
			errorResultMap.put("error", "해당 게시글이 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Comments comment = commentsServiceImpl.saveComment(post, profile, commentsSaveDTO);
		resultMap.put("postId", post.getId());
		resultMap.put("commentId", comment.getId());
		resultMap.put("nickname", profile.getNickname());
		resultMap.put("message", "create");

		return ResponseEntity.status(HttpStatus.CREATED).body(resultMap);
	}

	@PatchMapping
	public ResponseEntity<?> updateComment(@RequestBody CommentsUpdateDTO commentsUpdateDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		Posts post;
		Profiles profile;
		Comments comment;

		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = "";
		if (getEmail.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		} else {
			return getEmail;
		}
		Optional<Users> user = usersRepository.findByEmail(email);
		if (user.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<Comments> comments = commentsRepository.findById(commentsUpdateDTO.getCommentId());
		if (comments.isPresent()) {
			comment = comments.get();
		} else {
			errorResultMap.put("error", "댓글이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());
		if (lastProfiles.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		if (!lastProfiles.get(0).getProfile().getNickname().equals(comment.getProfile().getNickname())) {
			errorResultMap.put("error", "댓글을 작성한 프로필이 아닙니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
		}

		if (!postsRepository.existsById(commentsUpdateDTO.getPostId())) {
			errorResultMap.put("error", "해당 게시글이 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		commentsServiceImpl.updateComment(commentsUpdateDTO, comment);

		resultMap.put("postId", comment.getPost().getId());
		resultMap.put("commentId", comment.getId());
		resultMap.put("content", comment.getContent());
		resultMap.put("message", "update");

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@DeleteMapping
	public ResponseEntity<?> deleteComment(@RequestBody CommentsDeleteDTO commentsDeleteDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		Posts post;
		Profiles profile;
		Comments comment;
		long postId;

		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = "";
		if (getEmail.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> body = getEmail.getBody();
			email = (String) body.get("email");
		} else {
			return getEmail;
		}
		Optional<Users> user = usersRepository.findByEmail(email);
		if (user.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 유저입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<Comments> comments = commentsRepository.findById(commentsDeleteDTO.getCommentId());
		if (comments.isPresent()) {
			comment = comments.get();
			postId = comment.getPost().getId();
		} else {
			errorResultMap.put("error", "댓글이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user.get());
		if (lastProfiles.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		if (!lastProfiles.get(0).getProfile().getNickname().equals(comment.getProfile().getNickname())) {
			errorResultMap.put("error", "댓글을 작성한 프로필이 아닙니다.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
		}
		if (!postsRepository.existsById(commentsDeleteDTO.getPostId())) {
			errorResultMap.put("error", "해당 게시글이 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		commentsServiceImpl.deleteComment(comment);

		resultMap.put("postId", postId);
		resultMap.put("message", "delete");

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}
}
