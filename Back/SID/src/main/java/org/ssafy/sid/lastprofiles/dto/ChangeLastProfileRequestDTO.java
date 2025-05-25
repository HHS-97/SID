package org.ssafy.sid.lastprofiles.dto;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChangeLastProfileRequestDTO {
	private String nickname;
}
