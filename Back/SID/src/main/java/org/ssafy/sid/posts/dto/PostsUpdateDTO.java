package org.ssafy.sid.posts.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PostsUpdateDTO {
	private long postId;
	private String content;
	private MultipartFile image=null;
}
