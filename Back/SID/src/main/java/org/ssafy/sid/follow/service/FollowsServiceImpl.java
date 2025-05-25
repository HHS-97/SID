package org.ssafy.sid.follow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ssafy.sid.follow.dto.FollowsDTO;
import org.ssafy.sid.follow.model.Follows;
import org.ssafy.sid.follow.model.FollowsRepository;
import org.ssafy.sid.images.service.ImageSaveServiceImpl;
import org.ssafy.sid.profiles.model.Profiles;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowsServiceImpl implements FollowsService {

	private final FollowsRepository followsRepository;
	private final ImageSaveServiceImpl imageSaveServiceImpl;

	@Override
	@Transactional
	public void addFollow(FollowsDTO followsDTO) {
		followsRepository.save(followsDTO.toEntity());
	}

	@Override
	@Transactional
	public void deleteFollow(Follows follow) {
		followsRepository.delete(follow);
	}

	@Override
	@Transactional
	public List<Map<String, Object>> getFollowings(Profiles profile, Profiles lastProfile) throws IOException {
		List<Follows> FollowingsList = followsRepository.findByFollower(profile);

		// 결과를 저장할 리스트 (Map의 key: 팔로워의 닉네임, value: 내가 그 팔로워를 팔로우하고 있는지 여부)
		List<Map<String, Object>> result = new ArrayList<>();

		for (Follows follow : FollowingsList) {
			Profiles follower = follow.getFollowing(); // 팔로워 프로필

			// 내가 해당 팔로잉을 팔로우하고 있는지 확인 (상호 팔로우 여부)
			boolean isFollowed = followsRepository.existsByFollowerAndFollowing(lastProfile, follower);

			// 결과 Map에 팔로워의 닉네임과 상호 팔로우 여부 저장
			Map<String, Object> followerInfo = new HashMap<>();
			followerInfo.put("nickname", follower.getNickname());
			followerInfo.put("profileImage", imageSaveServiceImpl.checkImage(follower.getProfileImage()));
			followerInfo.put("isFollowed", isFollowed);

			result.add(followerInfo);
		}

		return result;
	}

	@Override
	@Transactional
	public List<Map<String, Object>> getFollowers(Profiles profile, Profiles lastProfile) throws IOException {
		// 주어진 프로필을 'following'으로 갖는 Follows 엔티티들 = 나를 따르는 사람들
		List<Follows> followersList = followsRepository.findByFollowing(profile);

		// 결과를 저장할 리스트 (Map의 key: 팔로워의 닉네임, value: 내가 그 팔로워를 팔로우하고 있는지 여부)
		List<Map<String, Object>> result = new ArrayList<>();

		for (Follows follow : followersList) {
			Profiles follower = follow.getFollower(); // 팔로워 프로필

			// 내가 해당 팔로워를 팔로우하고 있는지 확인 (상호 팔로우 여부)
			boolean isFollowed = followsRepository.existsByFollowerAndFollowing(lastProfile, follower);

			// 결과 Map에 팔로워의 닉네임과 상호 팔로우 여부 저장
			Map<String, Object> followerInfo = new HashMap<>();
			followerInfo.put("nickname", follower.getNickname());
			followerInfo.put("profileImage", imageSaveServiceImpl.checkImage(follower.getProfileImage()));
			followerInfo.put("isFollowed", isFollowed);

			result.add(followerInfo);
		}

		return result;
	}
}
