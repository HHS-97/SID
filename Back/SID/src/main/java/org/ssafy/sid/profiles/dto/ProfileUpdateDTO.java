package org.ssafy.sid.profiles.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateDTO {
	private String nickname = null;
	private String description = null;
	private MultipartFile profileImage = null;
	private List<Long> interestCategoryId = new ArrayList<>();  // 카테고리 만들면 추가
}
