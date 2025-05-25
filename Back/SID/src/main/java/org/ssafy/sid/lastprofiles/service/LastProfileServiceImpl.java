package org.ssafy.sid.lastprofiles.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ssafy.sid.exception.ProfileNotFoundException;
import org.ssafy.sid.lastprofiles.dto.LastProfileSaveDTO;
import org.ssafy.sid.lastprofiles.dto.LastProfileUpdateDTO;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.lastprofiles.model.LastProfilesRepository;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.profiles.model.ProfilesRepository;
import org.ssafy.sid.users.model.Users;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LastProfileServiceImpl implements LastProfileService {

	private final LastProfilesRepository lastProfilesRepository;
	private final ProfilesRepository profilesRepository;

	@Override
	@Transactional
	public void createLastProfiles(LastProfileSaveDTO lastProfileSaveDTO) {
		LastProfiles lastProfile = lastProfileSaveDTO.toEntity();
		lastProfilesRepository.save(lastProfile);
	}

	@Override
	@Transactional
	public String updateLastProfiles(LastProfileUpdateDTO lastProfileUpdateDTO, Users user) {
		LastProfiles lastProfile = lastProfilesRepository.findByUser(user).get(0);
		lastProfile.update(lastProfileUpdateDTO);
		return lastProfile.getProfile().getNickname();
	}

	@Override
	@Transactional
	public void deleteLastProfiles(LastProfiles lastProfile) {
		lastProfilesRepository.delete(lastProfile);
	}

	@Override
	@Transactional
	public Boolean getLastProfile(Users user) {
		List<LastProfiles> lastProfiles = lastProfilesRepository.findByUser(user);
		if (lastProfiles.isEmpty()) {
			// 없으면 새로생성해야되는거로 가게 나중에 얘기해보기
			List<Profiles> profilesList = profilesRepository.findByUser(user);

			if (profilesList.isEmpty()) {
				return true;
			} else {
				createLastProfiles(LastProfileSaveDTO.builder()
						.user(user)
						.profile(profilesList.get(0))
						.build());
			}
		}  else if (lastProfiles.size() > 1) {
			// 만약 lastProfile이 여러개 생긴 경우 0번 제외 삭제
			LastProfiles remainLastProfiles = lastProfiles.get(0);
			lastProfiles.remove(0);
			lastProfilesRepository.deleteAll(lastProfiles);
		} else {
			Profiles lastProfile = lastProfiles.get(0).getProfile();
			deleteLastProfiles(lastProfiles.get(0));
			createLastProfiles(LastProfileSaveDTO.builder()
					.user(user)
					.profile(lastProfile)
					.build());
		}
		return false;
	}
}
