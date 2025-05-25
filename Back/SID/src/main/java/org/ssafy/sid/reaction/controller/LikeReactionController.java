package org.ssafy.sid.reaction.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ssafy.sid.comments.model.Comments;
import org.ssafy.sid.comments.model.CommentsRepository;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.lastprofiles.model.LastProfilesRepository;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.posts.model.PostsRepository;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.profiles.model.ProfilesRepository;
import org.ssafy.sid.reaction.dto.CommentLikeReactionsDTO;
import org.ssafy.sid.reaction.dto.PostLikeReactionsDTO;
import org.ssafy.sid.reaction.model.CommentReactions;
import org.ssafy.sid.reaction.model.CommentReactionsRepository;
import org.ssafy.sid.reaction.model.PostReactions;
import org.ssafy.sid.reaction.model.PostReactionsRepository;
import org.ssafy.sid.reaction.service.ReactionsServiceImpl;
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
@RequestMapping("/api/like")
public class
LikeReactionController {

	private final JwtUtil jwtUtil;
	private final UsersRepository usersRepository;
	private final ProfilesRepository profilesRepository;
	private final PostReactionsRepository postReactionsRepository;
	private final PostsRepository postsRepository;
	private final ReactionsServiceImpl reactionsServiceImpl;
	private final CommentsRepository commentsRepository;
	private final CommentReactionsRepository commentReactionsRepository;
	private final JwtServiceImpl jwtServiceImpl;
	private final LastProfilesRepository lastProfilesRepository;

	@PostMapping("/posts")
	public ResponseEntity<?> postsLike(@RequestBody PostLikeReactionsDTO postLikeReactionsDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		PostReactions postReaction;

		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = null;
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

		List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user.get());
		if (lastProfile.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Profiles profiles = lastProfile.get(0).getProfile();
		if (!profilesRepository.existsById(profiles.getId())) {
			errorResultMap.put("error", "존재하지 않는 프로필입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<Posts> posts = postsRepository.findById(postLikeReactionsDTO.getPostId());
		if (posts.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 게시글입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<PostReactions> postReactions = postReactionsRepository.findByPostAndProfile(posts.get(), profiles);
		if (postReactions.isPresent()) {
			if (!postLikeReactionsDTO.getIsLike() && !postReactions.get().getPositive()) {
				// 저장된 데이터가 있으면 update로
				postReaction = reactionsServiceImpl.updatePostLikeReactions(postReactions.get());
			} else {
				errorResultMap.put("error", "데이터가 잘못됐습니다.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
			}
		} else {
			if (!postLikeReactionsDTO.getIsLike()) {
				// 저장된 데이터가 없으면 create로
				postReaction = reactionsServiceImpl.createPostLikeReactions(posts.get(), profiles);
			} else {
				errorResultMap.put("error", "데이터가 잘못됐습니다.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
			}
		}

		long dislikeCount = postReactionsRepository.countByPostAndPositive(postReaction.getPost(), false);
		resultMap.put("dislikeCount", dislikeCount);
		long likeCount = postReactionsRepository.countByPostAndPositive(postReaction.getPost(), true);
		resultMap.put("likeCount", likeCount);

		resultMap.put("postId", postReaction.getPost().getId());
		resultMap.put("reaction", "true");
		resultMap.put("message", "successful");

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@DeleteMapping("/posts")
	public ResponseEntity<?> deletePostLike(@RequestBody PostLikeReactionsDTO postLikeReactionsDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();

//		System.out.println(postLikeReactionsDTO.getIsLike());

		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = null;
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

		List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user.get());
		if (lastProfile.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Profiles profiles = lastProfile.get(0).getProfile();
		if (!profilesRepository.existsById(profiles.getId())) {
			errorResultMap.put("error", "존재하지 않는 프로필입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<Posts> posts = postsRepository.findById(postLikeReactionsDTO.getPostId());
		if (posts.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 게시글입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<PostReactions> postReactions = postReactionsRepository.findByPostAndProfile(posts.get(), profiles);
		if (postReactions.isPresent()) {
			if (postLikeReactionsDTO.getIsLike() && postReactions.get().getPositive()) {
				reactionsServiceImpl.deletePostReactions(postReactions.get());
			} else {
				errorResultMap.put("error", "데이터가 잘못됐습니다.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
			}
		} else {
			errorResultMap.put("error", "데이터가 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		long dislikeCount = postReactionsRepository.countByPostAndPositive(postReactions.get().getPost(), false);
		resultMap.put("dislikeCount", dislikeCount);
		long likeCount = postReactionsRepository.countByPostAndPositive(postReactions.get().getPost(), true);
		resultMap.put("likeCount", likeCount);

		resultMap.put("postId", postReactions.get().getPost().getId());
		resultMap.put("reaction", null);
		resultMap.put("message", "successful");
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@PostMapping("/comments")
	public ResponseEntity<?> commentsLike(@RequestBody CommentLikeReactionsDTO commentLikeReactionsDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		CommentReactions commentReaction;

		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = null;
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

		List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user.get());
		if (lastProfile.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Profiles profiles = lastProfile.get(0).getProfile();
		if (!profilesRepository.existsById(profiles.getId())) {
			errorResultMap.put("error", "존재하지 않는 프로필입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<Comments> comments = commentsRepository.findById(commentLikeReactionsDTO.getPostId());
		if (comments.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 댓글입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<CommentReactions> commentReactions = commentReactionsRepository.findByCommentAndProfile(comments.get(), profiles);
		if (commentReactions.isPresent()) {
			if (!commentLikeReactionsDTO.getIsLike() && !commentReactions.get().getPositive()) {
				// 저장된 데이터가 있으면 update로
				commentReaction = reactionsServiceImpl.updateCommentLikeReactions(commentReactions.get());
			} else {
				errorResultMap.put("error", "데이터가 잘못됐습니다.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
			}
		} else {
			if (!commentLikeReactionsDTO.getIsLike()) {
				// 저장된 데이터가 없으면 create로
				commentReaction = reactionsServiceImpl.createCommentLikeReactions(comments.get(), profiles);
			} else {
				errorResultMap.put("error", "데이터가 잘못됐습니다.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
			}
		}

		long dislikeCount = commentReactionsRepository.countByCommentAndPositive(commentReaction.getComment(), false);
		resultMap.put("dislikeCount", dislikeCount);
		long likeCount = commentReactionsRepository.countByCommentAndPositive(commentReaction.getComment(), true);
		resultMap.put("likeCount", likeCount);

		resultMap.put("commentId", commentReaction.getComment().getId());
		resultMap.put("reaction", "true");
		resultMap.put("message", "successful");

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@DeleteMapping("/comments")
	public ResponseEntity<?> deleteCommentsLike(@RequestBody CommentLikeReactionsDTO commentLikeReactionsDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();

		ResponseEntity<Map<String, Object>> getEmail = jwtServiceImpl.getEmail(request);
		String email = null;
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

		List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user.get());
		if (lastProfile.isEmpty()) {
			errorResultMap.put("error", "최근 접속 프로필 기록이 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Profiles profiles = lastProfile.get(0).getProfile();
		if (!profilesRepository.existsById(profiles.getId())) {
			errorResultMap.put("error", "존재하지 않는 프로필입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<Comments> comments = commentsRepository.findById(commentLikeReactionsDTO.getPostId());
		if (comments.isEmpty()) {
			errorResultMap.put("error", "존재하지 않는 댓글입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		Optional<CommentReactions> commentReactions = commentReactionsRepository.findByCommentAndProfile(comments.get(), profiles);
		if (commentReactions.isPresent()) {
			if (commentLikeReactionsDTO.getIsLike() && commentReactions.get().getPositive()) {
				reactionsServiceImpl.deleteCommentReactions(commentReactions.get());
			} else {
				errorResultMap.put("error", "데이터가 잘못됐습니다.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResultMap);
			}
		} else {
			errorResultMap.put("error", "데이터가 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		long dislikeCount = commentReactionsRepository.countByCommentAndPositive(commentReactions.get().getComment(), false);
		resultMap.put("dislikeCount", dislikeCount);
		long likeCount = commentReactionsRepository.countByCommentAndPositive(commentReactions.get().getComment(), true);
		resultMap.put("likeCount", likeCount);

		resultMap.put("commentId", commentReactions.get().getComment().getId());
		resultMap.put("reaction", null);
		resultMap.put("message", "successful");
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}


}
