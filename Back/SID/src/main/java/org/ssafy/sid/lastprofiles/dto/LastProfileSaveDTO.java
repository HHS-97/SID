package org.ssafy.sid.lastprofiles.dto;

import lombok.*;
import org.ssafy.sid.lastprofiles.model.LastProfiles;
import org.ssafy.sid.profiles.model.Profiles;
import org.ssafy.sid.users.model.Users;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LastProfileSaveDTO {
	private Users user;
	private Profiles profile;

	public LastProfiles toEntity() {

		return LastProfiles.builder()
				.user(this.user)
				.profile(this.profile)
				.build();
	}
}
