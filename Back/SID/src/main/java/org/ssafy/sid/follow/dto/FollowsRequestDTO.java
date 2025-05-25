package org.ssafy.sid.follow.dto;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FollowsRequestDTO {
	private Boolean isFollowed;
	private String followNickname;
}
