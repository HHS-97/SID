package org.ssafy.sid.profiles.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ssafy.sid.categories.dto.InterestCategoryGetDTO;
import org.ssafy.sid.categories.dto.InterestCategorySaveDTO;
import org.ssafy.sid.categories.model.Categories;
import org.ssafy.sid.categories.model.CategoriesRepository;
import org.ssafy.sid.categories.model.InterestCategoriesRepository;
import org.ssafy.sid.categories.service.CategoriesServiceImpl;
import org.ssafy.sid.exception.NicknameDuplicatedException;
import org.ssafy.sid.exception.ProfileMaxException;
import org.ssafy.sid.exception.ProfileNotFoundException;
import org.ssafy.sid.exception.UserNotFoundException;
import org.ssafy.sid.follow.model.Follows;
import org.ssafy.sid.follow.model.FollowsRepository;
import org.ssafy.sid.images.service.ImageSaveServiceImpl;
import org.ssafy.sid.lastprofiles.dto.LastProfileUpdateDTO;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.lastprofiles.model.LastProfilesRepository;
import org.ssafy.sid.lastprofiles.service.LastProfileServiceImpl;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.posts.model.PostsRepository;
import org.ssafy.sid.profiles.dto.ProfileDetailDTO;
import org.ssafy.sid.profiles.dto.ProfileListDTO;
import org.ssafy.sid.profiles.dto.ProfileSaveDTO;
import org.ssafy.sid.profiles.dto.ProfileUpdateDTO;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.profiles.model.ProfilesRepository;
import org.ssafy.sid.reaction.model.CommentReactionsRepository;
import org.ssafy.sid.reaction.model.PostReactionsRepository;
import org.ssafy.sid.users.UsersRepository;
import org.ssafy.sid.users.model.Users;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

	private final ProfilesRepository profilesRepository;

	private final ImageSaveServiceImpl imageSaveService;
	private final LastProfilesRepository lastProfilesRepository;
	private final LastProfileServiceImpl lastProfileServiceImpl;
	private final UsersRepository usersRepository;
	private final FollowsRepository followsRepository;
	private final PostsRepository postsRepository;
	private final CategoriesServiceImpl categoriesServiceImpl;
	private final CategoriesRepository categoriesRepository;
	private final InterestCategoriesRepository interestCategoriesRepository;
	private final PostReactionsRepository postReactionsRepository;
	private final CommentReactionsRepository commentReactionsRepository;
	private final ImageSaveServiceImpl imageSaveServiceImpl;

	@Override
	@Transactional
	public Profiles createProfile(ProfileSaveDTO profileSaveDTO) throws IOException {

		Profiles profile = profileSaveDTO.toEntity(imageSaveService.saveImage(profileSaveDTO.getProfileImage(), "profile"));
		profilesRepository.save(profile);

		// 흥미 카테고리 저장하는 것도 만들어야함
		for (Long categoryId : profileSaveDTO.getInterestCategoryId()) {
			Optional<Categories> categories = categoriesRepository.findById(categoryId);
			if (categories.isPresent()) {
				categoriesServiceImpl.addInterestCategory(InterestCategorySaveDTO.builder().profile(profile).category(categories.get()).build());
			} else {
				throw new NullPointerException("카테고리를 찾을 수 없습니다. : " + categoryId);
			}
		}

		return profile;
	}

	@Override
	@Transactional
	public ProfileDetailDTO profileDetail(Profiles profile) {
		long followerCount = followsRepository.countByFollowing(profile);
		long followingCount = followsRepository.countByFollower(profile);
		long postsCount = postsRepository.countByProfile(profile);
		List<InterestCategoryGetDTO> interestCategoryGetDTOList = categoriesServiceImpl.getInterestCategories(profile);
		String profileImageUrl = profile.getProfileImage();
		if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
			// 만약 profileImageUrl이 상대 경로라면, 현재 작업 디렉토리(user.dir) 기준으로 File 객체 생성
			File imageFile = new File(System.getProperty("user.dir"), profileImageUrl);
			if (!imageFile.exists()) {
				profileImageUrl = "";
			}
		} else {
			profileImageUrl = "";
		}

		return ProfileDetailDTO.builder()
				.nickname(profile.getNickname())
				.description(profile.getDescription())
				.interestCategories(interestCategoryGetDTOList)
				.profileImageUrl(profileImageUrl)
				.followerCount(followerCount)
				.followingCount(followingCount)
				.postsCount(postsCount)
				.build();
	}

	@Override
	@Transactional
	public String updateProfile(ProfileUpdateDTO profileUpdateDTO, Profiles profile) throws IOException {
		String url = null;
		if (profileUpdateDTO.getProfileImage() != null) {
			url = imageSaveService.saveImage(profileUpdateDTO.getProfileImage(), "profile");
			// 프로필 이미지 삭제
			File file = new File(profile.getProfileImage());
			if (file.exists()) {
				boolean deleted = file.delete();
				if (!deleted) {
					// 삭제 실패시 예외를 던지거나 로그를 기록할 수 있습니다.
					throw new RuntimeException("파일 삭제에 실패했습니다: " + profile.getProfileImage());
				}
			}
		}

		interestCategoriesRepository.deleteAllByProfile(profile);

		if (profileUpdateDTO.getInterestCategoryId() != null && !profileUpdateDTO.getInterestCategoryId().isEmpty()) {
			for (Long categoryId : profileUpdateDTO.getInterestCategoryId()) {
				Optional<Categories> categories = categoriesRepository.findById(categoryId);
				if (categories.isPresent()) {
					categoriesServiceImpl.addInterestCategory(InterestCategorySaveDTO.builder().profile(profile).category(categories.get()).build());
				} else {
					throw new NullPointerException("카테고리를 찾을 수 없습니다. : " + categoryId);
				}
			}
		}
		
		profile.update(profileUpdateDTO, url);
		return profile.getNickname();
	}

	@Override
	@Transactional
	public void deleteProfile(String nickname) {
		Optional<Profiles> profile = profilesRepository.findByNickname(nickname);
		if (profile.isPresent()) {
			// 프로필 이미지 삭제
			File file = new File(profile.get().getProfileImage());
			if (file.exists()) {
				boolean deleted = file.delete();
				if (!deleted) {
					// 삭제 실패시 예외를 던지거나 로그를 기록할 수 있습니다.
					throw new RuntimeException("파일 삭제에 실패했습니다: " + profile.get().getProfileImage());
				}
			}

			// 라스트 프로필 삭제
			Users user = profile.get().getUser();
			Optional<LastProfiles> lastProfile = lastProfilesRepository.findByProfile(profile.get());
			if (lastProfile.isPresent()) {
				List<Profiles> profilesList = profilesRepository.findByUser(user);
				profilesList.remove(profile.get());

				if (profilesList.isEmpty()) {
					lastProfileServiceImpl.deleteLastProfiles(lastProfile.get());
				} else {
					LastProfileUpdateDTO lastProfileUpdateDTO = LastProfileUpdateDTO.builder().profile(profilesList.get(0)).build();
					lastProfileServiceImpl.updateLastProfiles(lastProfileUpdateDTO, user);
				}
			}

			// 팔로워, 팔로잉 삭제
			followsRepository.deleteAllByFollower(profile.get());
			followsRepository.deleteAllByFollowing(profile.get());

			// 리액션 삭제
			postReactionsRepository.deleteAllByProfile(profile.get());
			commentReactionsRepository.deleteAllByProfile(profile.get());

			profilesRepository.delete(profile.get());
		}
	}

	@Override
	@Transactional
	public ProfileListDTO profileList(String email) throws IOException {
		Users user = usersRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저입니다."));
		List<Profiles> profilesList = profilesRepository.findByUser(user);
		List<LastProfiles> lastProfile = lastProfilesRepository.findByUser(user);

		List<Map<String, Object>> resultList = new ArrayList<>();
		for (Profiles profiles : profilesList) {
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("nickname", profiles.getNickname());
			resultMap.put("profileImage", imageSaveServiceImpl.checkImage(profiles.getProfileImage()));
			resultList.add(resultMap);
		}

		Map<String, Object> lastProfileMap = new HashMap<>();
		if (!lastProfile.isEmpty()) {
			lastProfileMap.put("nickname", lastProfile.get(0).getProfile().getNickname());
			lastProfileMap.put("profileImage", imageSaveServiceImpl.checkImage(lastProfile.get(0).getProfile().getProfileImage()));
		}

		return ProfileListDTO.builder()
				.profiles(resultList)
				.lastProfile(lastProfileMap)
				.build();
	}
}
