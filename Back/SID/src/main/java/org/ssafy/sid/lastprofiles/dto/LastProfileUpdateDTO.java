package org.ssafy.sid.lastprofiles.dto;

import lombok.*;
import org.ssafy.sid.profiles.model.Profiles;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LastProfileUpdateDTO {
	private Profiles profile;
}
