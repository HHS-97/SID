package org.ssafy.sid.reaction.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class CommentDislikeReactionsDTO {
	private Boolean isDislike;
	private long postId;
	private long commentId;
}
