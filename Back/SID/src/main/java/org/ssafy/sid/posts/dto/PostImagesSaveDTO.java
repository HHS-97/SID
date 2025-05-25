package org.ssafy.sid.posts.dto;

import lombok.*;
import org.ssafy.sid.posts.model.PostImages;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.profiles.model.Profiles;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostImagesSaveDTO {
	private Posts post;
	private String image;
	private long order;

	public PostImages toEntity() {

		return PostImages.builder()
				.post(this.post)
				.image(this.image)
				.imageOrder(this.order)
				.build();
	}
}
