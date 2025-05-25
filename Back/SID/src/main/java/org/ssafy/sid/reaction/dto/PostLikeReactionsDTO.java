package org.ssafy.sid.reaction.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class PostLikeReactionsDTO {
	private Boolean isLike;
	private long postId;
}
