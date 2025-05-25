package org.ssafy.sid.comments.dto;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentsDeleteDTO {
//	private String nickname;
	private long postId;
	private long commentId;
}
