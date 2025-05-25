package org.ssafy.sid.curating.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.ssafy.sid.curating.model.CuratingData;
import org.ssafy.sid.curating.model.CuratingDataRepository;
import org.ssafy.sid.posts.dto.PostsGetDTO;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.posts.model.PostsRepository;
import org.ssafy.sid.posts.service.PostsServiceImpl;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.reaction.model.PostReactions;
import org.ssafy.sid.reaction.model.PostReactionsRepository;

import java.beans.Transient;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class CuratingServiceImpl implements CuratingService {

	private final WebClient webClient;
	private final PostsServiceImpl postsServiceImpl;
	private final CuratingDataRepository curatingDataRepository;
	private final PostsRepository postsRepository;
	private final PostReactionsRepository postReactionsRepository;

	@Override
	@Transactional
	public List<PostsGetDTO> getCuratingPosts(int page, String uri, List<Long> postIds, Profiles profile) throws IOException {
		String externalUrl;
		List<PostReactions> postReactions = postReactionsRepository.findByPositiveAndProfile(false, profile);
		if (postIds == null) {
			postIds = new ArrayList<>();
		}
		if (!postReactions.isEmpty()) {
			for (PostReactions postReaction : postReactions) {
				postIds.add(postReaction.getPost().getId());
			}
		}
		// 외부 API 호출 URL 구성
		if (postIds == null || postIds.isEmpty()) {
			externalUrl = UriComponentsBuilder.fromUriString(uri)
					.queryParam("profile_id", profile.getId())
					.queryParam("total_num", 20)
					.build()
					.toUriString();
		} else {
			externalUrl = UriComponentsBuilder.fromUriString(uri)
					.queryParam("profile_id", profile.getId())
					.queryParam("total_num", 20)
					.queryParam("viewed_posts", postIds)
					.build()
					.toUriString();
		}

		// WebClient를 이용한 비동기 HTTP GET 요청
		long[] result = webClient.get()
				.uri(externalUrl)
				.retrieve()
				.bodyToMono(long[].class)
				.block();

		log.debug(Arrays.toString(result));
//		System.out.println(Arrays.toString(result));

		List<PostsGetDTO> resultList = new ArrayList<>();

		for (long id : result) {
			Optional<Posts> posts = postsRepository.findById(id);
			Posts post = null;

			try{
				if (posts.isPresent()) {
					post = posts.get();
					List<CuratingData> curatingData = curatingDataRepository.findByProfileAndPostAndType(profile, post, 'R');
					if (!curatingData.isEmpty()) {
						curatingData.get(0).updateUpdateCount();
					} else {
						curatingDataRepository.save(CuratingData.builder().post(post).profile(profile).type('R').build());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
//				System.out.println("프로필 : " + profile);
//				System.out.println("포스트  : " + post);
			}
			
			resultList.add(postsServiceImpl.getOthreOne(id, profile));
		}

		return resultList;
	}
}
