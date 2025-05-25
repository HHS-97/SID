package org.ssafy.sid.comments.dto;

import lombok.*;
import org.ssafy.sid.comments.model.Comments;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.profiles.model.Profiles;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentsSaveDTO {
//	private String nickname;
	private String content;
	private long postId;

	public Comments toEntity(Profiles profile, Posts post) {
		return Comments.builder()
				.profile(profile)
				.post(post)
				.content(this.content)
				.build();
	}
}
