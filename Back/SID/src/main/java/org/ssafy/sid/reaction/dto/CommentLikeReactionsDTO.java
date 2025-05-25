package org.ssafy.sid.reaction.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class CommentLikeReactionsDTO {
	private Boolean isLike;
	private long postId;
	private long commentId;
}
