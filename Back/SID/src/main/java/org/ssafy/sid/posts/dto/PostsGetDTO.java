package org.ssafy.sid.posts.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PostsGetDTO {
	private Long postId;
	private Map<String, Object> writer;
	private String content;
	private String image;
	private String createdAt;
	private String time; // 시간 차 (1일 전)
	private long likeCount;
	private long dislikeCount;
	private long commentCount;
	private String reaction;
}
