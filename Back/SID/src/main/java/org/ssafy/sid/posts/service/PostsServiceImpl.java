package org.ssafy.sid.posts.service;

import jakarta.persistence.Cacheable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.ssafy.sid.comments.model.Comments;
import org.ssafy.sid.comments.model.CommentsRepository;
import org.ssafy.sid.comments.service.CommentsServiceImpl;
import org.ssafy.sid.curating.model.CuratingData;
import org.ssafy.sid.curating.model.CuratingDataRepository;
import org.ssafy.sid.follow.model.Follows;
import org.ssafy.sid.follow.model.FollowsRepository;
import org.ssafy.sid.images.service.ImageSaveServiceImpl;
import org.ssafy.sid.posts.dto.*;
import org.ssafy.sid.posts.model.PostImages;
import org.ssafy.sid.posts.model.PostImagesRepository;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.posts.model.PostsRepository;
import org.ssafy.sid.profiles.dto.ProfileListDTO;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.profiles.model.ProfilesRepository;
import org.ssafy.sid.reaction.model.CommentReactions;
import org.ssafy.sid.reaction.model.CommentReactionsRepository;
import org.ssafy.sid.reaction.model.PostReactions;
import org.ssafy.sid.reaction.model.PostReactionsRepository;
import org.ssafy.sid.users.model.Users;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostsServiceImpl implements PostsService {

	private final PostsRepository postsRepository;
	private final ProfilesRepository profilesRepository;
	private final ImageSaveServiceImpl imageSaveServiceImpl;
	private final PostImagesRepository postImagesRepository;
	private final PostReactionsRepository postReactionsRepository;
	private final CommentsRepository commentsRepository;
	private final CommentReactionsRepository commentReactionsRepository;
	private final CommentsServiceImpl commentsServiceImpl;
	private final FollowsRepository followsRepository;
	private final CuratingDataRepository curatingDataRepository;
	private final WebClient webClient;

	@Override
	@Transactional
	public Posts createPost(PostsSaveDTO postsSaveDTO, Profiles profile) throws IOException {
		Posts post = postsSaveDTO.toEntity(profile);
		postsRepository.save(post);
		int order = 0;
		
		// 이미지 저장
		PostImagesSaveDTO postImagesSaveDTO = PostImagesSaveDTO.builder()
				.post(post)
				.image(imageSaveServiceImpl.saveImage(postsSaveDTO.getImage(), "post"))
				.order(order++)
			.build();

		PostImages postImage = postImagesSaveDTO.toEntity();

		postImagesRepository.save(postImage);

		return post;
	}

	@Override
	@Transactional
	public void updatePost(PostsUpdateDTO postsUpdateDTO, Posts post) throws IOException {

		// 이미지 저장
		if (postsUpdateDTO.getImage() != null) {
			// 게시글과 연결된 이미지 전부 삭제
			Optional<PostImages> postImages = postImagesRepository.findByPost(post);
			postImages.ifPresent(postImagesRepository::delete);
			int order = 0;

			if (!postsUpdateDTO.getImage().isEmpty()) {
				PostImagesSaveDTO postImagesSaveDTO = PostImagesSaveDTO.builder()
						.post(post)
						.image(imageSaveServiceImpl.saveImage(postsUpdateDTO.getImage(), "post"))
						.order(order++)
						.build();

				PostImages postImage = postImagesSaveDTO.toEntity();

				postImagesRepository.save(postImage);
			}
		}

		post.update(postsUpdateDTO);
	}

	@Override
	public void deletePost(Posts post) throws IOException {
		// 게시글과 연결된 이미지 전부 삭제
		Optional<PostImages> postImages = postImagesRepository.findByPost(post);
		// 각 이미지 엔티티에 저장된 경로를 통해 실제 파일을 삭제
		if (postImages.isPresent()) {
			PostImages image = postImages.get();
			// image.getImage()가 파일 경로(예: "/uploads/2025/02/04/filename.jpg")라고 가정
			File file = new File(image.getImage());
			if (file.exists()) {
				boolean deleted = file.delete();
				if (!deleted) {
					// 삭제 실패시 예외를 던지거나 로그를 기록할 수 있습니다.
					throw new RuntimeException("파일 삭제에 실패했습니다: " + image.getImage());
				}
			}
		}

		postImagesRepository.delete(postImages.get());

		// PostReaction 삭제
		postReactionsRepository.deleteAllByPost(post);

		// comments 삭제 메서드
		List<Comments> commentsList = commentsRepository.findAllByPost(post);
		for (Comments comment : commentsList) {
			commentsServiceImpl.deleteComment(comment);
		}

		List<CuratingData> curatingDataList = curatingDataRepository.findAllByPost(post);
		if (curatingDataList != null && curatingDataList.size() > 0) {
			curatingDataRepository.deleteAll(curatingDataList);
		}

		// 게시글 삭제
		postsRepository.delete(post);
	}

	@Override
	@Transactional
	public Page<PostsGetDTO> getPosts (int page, Profiles profile, int type, List<Long> postIds) {
		Page<Posts> postPage;

		if (type == 2) {
			PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("id").descending());
			postPage = postsRepository.findAllPostsWithRandomNumber(pageRequest, postIds);
		} else {
			PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("id").descending());
			postPage = postsRepository.findAll(pageRequest);
		}

		return postPage.map(post -> {
			try {
				return convertToDTO(post, profile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	@Transactional
	public Page<PostsGetDTO> getProfilePosts (int page, Profiles myProfile, Profiles thisProfile) {
		PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("id").descending());

		Page<Posts> postPage = postsRepository.findByProfile(thisProfile, pageRequest);

//		System.out.println("Repository returned elements: " + postPage.getContent().size());

		return postPage.map(post -> {
			try {
				return convertToDTO(post, myProfile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	@Transactional
	public Page<PostsGetDTO> getFollowPosts (int page, Profiles myProfile) {
		PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("id").descending());
		List<Follows> following = followsRepository.findByFollower(myProfile);
		List<Profiles> followedProfiles = following.stream()
				.map(Follows::getFollowing)
				.toList();

		if (followedProfiles.isEmpty()) {
			return Page.empty(pageRequest);
		}

		Page<Posts> postPage = postsRepository.findByProfileIn(followedProfiles, pageRequest);

		return postPage.map(post -> {
			try {
				return convertToDTO(post, myProfile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@SneakyThrows
	private PostsGetDTO convertToDTO(Posts post, Profiles profile) throws IOException {
		PostsGetDTO dto = new PostsGetDTO();
		Optional<PostImages> postImages = postImagesRepository.findByPostId(post.getId());
		String image = null;
		if (postImages.isPresent()) {
			image = postImages.get().getImage();
		}
		long likeCount = postReactionsRepository.countByPostAndPositive(post, true);
		long disLikeCount = postReactionsRepository.countByPostAndPositive(post, false);
		long commentCount = commentsRepository.countByPostId(post.getId());
		Optional<PostReactions> postReactions = profile != null ? postReactionsRepository.findByPostAndProfile(post, profile) : Optional.empty();
		Map<String, Object> writerMap = new HashMap<>();

		if (image != null && !image.isEmpty()) {
			// 만약 profileImageUrl이 상대 경로라면, 현재 작업 디렉토리(user.dir) 기준으로 File 객체 생성
			File imageFile = new File(System.getProperty("user.dir"), image);
			if (!imageFile.exists()) {
				image = "";
			}
		} else {
			image = "";
		}

		dto.setPostId(post.getId());
		if (post.getProfile() == null) {
			writerMap.put("nickname", "알 수 없음");
			writerMap.put("profileImage", "");
			dto.setWriter(writerMap);
		} else {
			writerMap.put("nickname", post.getProfile().getNickname());
			writerMap.put("profileImage", imageSaveServiceImpl.checkImage(post.getProfile().getProfileImage()));
			dto.setWriter(writerMap);
		}
		dto.setContent(post.getContent());
		dto.setImage(image);
		dto.setCreatedAt(post.getCreatedAt());
		dto.setTime(calculateTimeDifference(post.getCreatedAt()));
		if (postReactions.isPresent()) {
			if (postReactions.get().getPositive()) {
				dto.setReaction("true");
			} else {
				dto.setReaction("false");
			}
		} else {
			dto.setReaction(null);
		}
		dto.setLikeCount(likeCount);
		dto.setDislikeCount(disLikeCount);
		dto.setCommentCount(commentCount);

		return dto;
	}

	private PostImagesGetDTO convertToImageDTO(PostImages postimage) throws IOException {
		PostImagesGetDTO dto = new PostImagesGetDTO();
		Optional<PostImages> postImages =  postImagesRepository.findByPostId(postimage.getId());

		dto.setImage(imageSaveServiceImpl.checkImage(postimage.getImage()));
		dto.setImageOrder(postimage.getImageOrder());

		return dto;
	}

	private String calculateTimeDifference(String createdAtStr) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		LocalDateTime createdAt;
		try {
			createdAt = LocalDateTime.parse(createdAtStr, formatter);
		} catch (DateTimeParseException e) {
			return "시간 정보 없음";
		}

		LocalDateTime now = LocalDateTime.now();
		Duration duration = Duration.between(createdAt, now);

		long days = duration.toDays();
		long hours = duration.toHours() % 24;
		long minutes = duration.toMinutes() % 60;

		if (days > 0) {
			return days + "일 전";
		} else if (hours > 0) {
			return hours + "시간 전";
		} else {
			return minutes + "분 전";
		}
	}

	@Override
	@Transactional
	public Page<PostsGetMoreDTO> getMorePosts (int page, Profiles profile, Posts post) {
//		System.out.println("Fetching page: " + page);
		PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("id").ascending());

		if (profile != null) {
			List<CuratingData> curatingData = curatingDataRepository.findByProfileAndPostAndType(profile, post, 'D');
			if (!curatingData.isEmpty()) {
				curatingData.get(0).updateUpdateCount();
			} else {
				curatingDataRepository.save(CuratingData.builder().post(post).profile(profile).type('D').build());
			}
		}

		Page<Comments> commentPage = commentsRepository.findByPost(post, pageRequest);

		return commentPage.map(comment -> {
			try {
				return convertToGetMoreDTO(comment, profile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private PostsGetMoreDTO convertToGetMoreDTO(Comments comment, Profiles profile) throws IOException {
		PostsGetMoreDTO dto = new PostsGetMoreDTO();
		long likeCount = commentReactionsRepository.countByCommentAndPositive(comment, true);
		long disLikeCount = commentReactionsRepository.countByCommentAndPositive(comment, false);
		Optional<CommentReactions> commentReactions = commentReactionsRepository.findByCommentAndProfile(comment, profile);
		Map<String, Object> writerMap = new HashMap<>();

		dto.setCommentId(comment.getId());
		if (comment.getProfile() == null) {
			writerMap.put("nickname", "알 수 없음");
			writerMap.put("profileImage", "");
			dto.setWriter(writerMap);
		} else {
			writerMap.put("nickname", comment.getProfile().getNickname());
			writerMap.put("profileImage", imageSaveServiceImpl.checkImage(comment.getProfile().getProfileImage()));
			dto.setWriter(writerMap);
		}
		dto.setContent(comment.getContent());
		dto.setCreatedAt(comment.getCreatedAt());
		dto.setTime(calculateTimeDifference(comment.getCreatedAt()));
		if (commentReactions.isPresent()) {
			if (commentReactions.get().getPositive()) {
				dto.setReaction("true");
			} else {
				dto.setReaction("false");
			}
		} else {
			dto.setReaction(null);
		}
		dto.setLikeCount(likeCount);
		dto.setDislikeCount(disLikeCount);

		return dto;
	}

	@Override
	@Transactional
	public ResponseEntity<Map<String, Object>> getSearch(String keyword, String type, int page, Profiles myProfile) throws IOException {
		int pageSize = 10;
		PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("id").descending());
		Map<String, Object> resultMap = new HashMap<>();
		List<PostsGetDTO> posts = new ArrayList<>();
		List<Map<String, Object>> profiles = new ArrayList<>();
		List<PostsGetDTO> profilePosts = new ArrayList<>();


		// 1. 게시글(피드) 검색: 콘텐츠에 검색어가 포함된 게시글
		if ("ALL".equalsIgnoreCase(type) || "FEED".equalsIgnoreCase(type)) {
			Page<Posts> postsPage = postsRepository.findByContentContainingIgnoreCase(keyword, pageRequest);
			posts = postsPage.map(post -> {
				try {
					return convertToDTO(post, myProfile);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}).getContent();
		}

		// 2. 프로필 검색: 닉네임에 검색어가 포함된 프로필을 찾은 후,
		//    이들 프로필이 작성한 게시글들을 최신순 정렬하여 페이지당 5개씩 반환
		if ("ALL".equalsIgnoreCase(type) || "PROFILE".equalsIgnoreCase(type)) {
			int profilePageSize = 5;
			PageRequest profilePageRequest = PageRequest.of(page, profilePageSize, Sort.by("id").descending());
			Page<Profiles> profilesPage = profilesRepository.findByNicknameContainingIgnoreCase(keyword, profilePageRequest);
			List<Profiles> foundProfiles = profilesPage.getContent();

			if (!foundProfiles.isEmpty()) {
				for (Profiles profile : foundProfiles) {
					Map<String, Object> profileMap = new HashMap<>();
					profileMap.put("nickname", profile.getNickname());
					profileMap.put("profileImage", imageSaveServiceImpl.checkImage(profile.getProfileImage()));

					profiles.add(profileMap);
				}
			}

			if (!foundProfiles.isEmpty()) {
				int profilePostsPageSize = 5;
				PageRequest profilePostsPageRequest = PageRequest.of(page, profilePostsPageSize, Sort.by("id").descending());
				Page<Posts> postsByProfilesPage = postsRepository.findByProfileIn(foundProfiles, profilePostsPageRequest);
				profilePosts = postsByProfilesPage.map(post -> {
					try {
						return convertToDTO(post, myProfile);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}).getContent();

				// 피드 게시글에 이미 포함된 게시글은 프로필 게시글에서 제거
				Set<Long> postIds = posts.stream()
						.map(PostsGetDTO::getPostId)
						.collect(Collectors.toSet());
				profilePosts = profilePosts.stream()
						.filter(dto -> !postIds.contains(dto.getPostId()))
						.collect(Collectors.toList());
			}
		}

		resultMap.put("posts", posts);
		resultMap.put("profiles", profiles);
		resultMap.put("profilePosts", profilePosts);

		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@Override
	@Transactional
	public Map<String, Object> getOne(long id, Profiles profile) throws IOException {
		Map<String, Object> resultMap = new HashMap<>();
		Optional<Posts> post = postsRepository.findById(id);
		if (post.isPresent()) {
			resultMap.put("post", convertToDTO(post.get(), profile));
		}
		return resultMap;
	}

	@Override
	@Transactional
	public PostsGetDTO getOthreOne(long id, Profiles profile) throws IOException {
		Map<String, Object> resultMap = new HashMap<>();
		Optional<Posts> post = postsRepository.findById(id);
		if (post.isPresent()) {
			PostsGetDTO result = convertToDTO(post.get(), profile);
			return result;
		}
		return null;
	}

	@Override
	@Transactional
	public List<PostsGetDTO> getTrendPosts(int page, List<Long> postIds, Profiles profile) throws IOException {
		List<Long> popularPostIds;
		if (postIds.isEmpty()) {
			List<Long> emptyPostIds = new ArrayList<>();
			emptyPostIds.add(-1L);
			popularPostIds = postsRepository.findPopularPostIds(emptyPostIds);
		} else {
			popularPostIds = postsRepository.findPopularPostIds(postIds);
		}
		
//		System.out.println(popularPostIds);

		List<PostsGetDTO> resultList = new ArrayList<>();

		for (long id : popularPostIds) {
			Optional<Posts> posts = postsRepository.findById(id);
			Posts post;
			if (posts.isPresent()) {
				post = posts.get();
				List<CuratingData> curatingData = curatingDataRepository.findByProfileAndPostAndType(profile, post, 'R');
				if (!curatingData.isEmpty()) {
					curatingData.get(0).updateUpdateCount();
				} else {
					curatingDataRepository.save(CuratingData.builder().post(post).profile(profile).type('R').build());
				}
			}
			resultList.add(getOthreOne(id, profile));
		}

		return resultList;
	}
}
