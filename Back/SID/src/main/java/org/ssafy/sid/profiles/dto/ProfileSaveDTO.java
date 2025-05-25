package org.ssafy.sid.profiles.dto;

import jakarta.persistence.Column;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.ssafy.sid.images.service.ImageSaveServiceImpl;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.users.model.Users;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSaveDTO {
	private Users user;
	private String nickname;
	private String description;
	private MultipartFile profileImage;
	@Builder.Default
	private List<Long> interestCategoryId = new ArrayList<>();

	public Profiles toEntity(String imageUrl) {

		return Profiles.builder()
				.user(this.user)
				.nickname(this.nickname)
				.description(this.description)
				.profileImage(imageUrl)  // 이미지 저장하고 url가져오는 기능 만들고 추가하자
				.build();
	}

}
