package org.ssafy.sid.reaction.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class PostDislikeReactionsDTO {
	private Boolean isDislike;
	private long postId;
}
