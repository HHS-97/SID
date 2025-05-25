package org.ssafy.sid.posts.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.profiles.model.Profiles;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostsSaveDTO {
	private String nickname;
	private String content;
	private MultipartFile image;

	public Posts toEntity(Profiles profile) {

		return Posts.builder()
				.profile(profile)
				.content(this.content)
				.build();
	}
}
