package org.ssafy.sid.reaction.dto;

import lombok.*;
import org.ssafy.sid.comments.model.Comments;
import org.ssafy.sid.posts.model.Posts;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.reaction.model.CommentReactions;
import org.ssafy.sid.reaction.model.PostReactions;

@Getter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class CommentReactionsSaveDTO {
	Comments comment;
	Profiles profile;
	Boolean positive;

	public CommentReactions toEntity() {
		return CommentReactions.builder()
				.comment(this.comment)
				.profile(this.profile)
				.positive(this.positive)
				.build();
	}
}
