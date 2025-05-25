package org.ssafy.sid.comments.dto;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentsUpdateDTO {
	private String content;
	private long postId;
	private long commentId;
}
