package org.ssafy.sid.follow.dto;

import lombok.*;
import org.ssafy.sid.follow.model.Follows;
import org.ssafy.sid.profiles.model.Profiles;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FollowsDTO {
	private Profiles follower;
	private Profiles following;

	public Follows toEntity() {
		return Follows.builder()
				.follower(this.follower)
				.following(this.following)
				.build();
	}
}
