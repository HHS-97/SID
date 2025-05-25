package org.ssafy.sid.posts.dto;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PostsDeleteDTO {
	private long postId;
	private String nickname;
}
