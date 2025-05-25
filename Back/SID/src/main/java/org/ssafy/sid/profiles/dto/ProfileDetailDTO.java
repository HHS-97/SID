package org.ssafy.sid.profiles.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import org.ssafy.sid.categories.dto.InterestCategoryGetDTO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDetailDTO {
	private String nickname;
	private String description;
	private String profileImageUrl;  // 일단 url형태
//	private File profileImage; // 파일형태
	@Builder.Default
	private List<InterestCategoryGetDTO> interestCategories = new ArrayList<>(); // 카테고리 만들때 같이만들기
	private long followerCount; // 팔로우 만들때 같이 만듦, 이 프로필의 팔로워 수
    private long followingCount; // 팔로잉 수
	private String isFollowed; // 타인의 프로필일 경우 팔로우 여부 팔로우 만들때 같이 만듦
	private long postsCount; // 작성한 게시글의 갯수 게시글 만들때 같이 만듦
}

