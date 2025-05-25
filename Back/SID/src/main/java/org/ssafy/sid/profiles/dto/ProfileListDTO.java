package org.ssafy.sid.profiles.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProfileListDTO {
	private List<Map<String, Object>> profiles;
	private Map<String, Object> lastProfile;
}
