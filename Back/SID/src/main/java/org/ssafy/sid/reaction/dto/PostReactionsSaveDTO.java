package org.ssafy.sid.reaction.dto;

import lombok.*;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.reaction.model.PostReactions;

@Getter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class PostReactionsSaveDTO {
	Posts post;
	Profiles profile;
	Boolean positive;

	public PostReactions toEntity() {
		return PostReactions.builder()
				.post(this.post)
				.profile(this.profile)
				.positive(this.positive)
				.build();
	}
}
